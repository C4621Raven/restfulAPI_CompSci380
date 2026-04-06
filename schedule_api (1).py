"""
Schedule RESTful API
====================
Serves course schedule data from Final_Draft_of_Schedule.xlsx

Endpoints:
  GET  /api/terms                     - List all available terms
  GET  /api/courses                   - List all courses (supports filters)
  GET  /api/courses/<sec_name>        - Get a specific course section
  POST /api/courses                   - Add a new course
  PUT  /api/courses/<sec_name>        - Update an existing course
  DELETE /api/courses/<sec_name>      - Delete a course

Query Parameters (for GET /api/courses):
  term       - Filter by term (e.g. 26FA, 26SU)
  dept       - Filter by department (e.g. MACS, ECBA)
  faculty    - Filter by faculty name
  status     - Filter by section status (e.g. A)
  room       - Filter by meeting room
  consent    - Filter by faculty consent required (Y/N)
  limit      - Max number of results (default: all)
  offset     - Pagination offset (default: 0)

Run:
  python schedule_api.py

Requirements:
  pip install flask pandas openpyxl
"""

import os
import pandas as pd
from flask import Flask, jsonify, request, abort

app = Flask(__name__)

# ── Data Loading ─────────────────────────────────────────────────────────────

EXCEL_PATH = os.path.join(os.path.dirname(__file__), "Final_Draft_of_Schedule.xlsx")

def load_data() -> pd.DataFrame:
    """Load and merge all sheets from the Excel file."""
    sheets = pd.read_excel(EXCEL_PATH, sheet_name=None)
    frames = []
    for term_label, df in sheets.items():
        df = df.copy()
        df["_sheet"] = term_label          # keep track of source sheet
        frames.append(df)
    combined = pd.concat(frames, ignore_index=True)
    # Normalise column names to snake_case
    combined.columns = [
        c.strip()
         .lower()
         .replace(" ", "_")
         .replace(".", "_")
         .replace("/", "_")
        for c in combined.columns
    ]
    # Fill NaN with None so JSON serialises cleanly
    combined = combined.where(pd.notnull(combined), None)
    return combined

df_global = load_data()

# ── Helpers ───────────────────────────────────────────────────────────────────

def df_to_records(df: pd.DataFrame) -> list[dict]:
    return df.to_dict(orient="records")

def find_course(sec_name: str) -> pd.Series | None:
    matches = df_global[df_global["sec_name"].str.lower() == sec_name.lower()]
    return None if matches.empty else matches.iloc[0]

# ── Routes ────────────────────────────────────────────────────────────────────

@app.route("/api/terms", methods=["GET"])
def get_terms():
    """Return all unique terms and how many courses are in each."""
    counts = df_global.groupby("term").size().reset_index(name="course_count")
    return jsonify({
        "terms": df_to_records(counts),
        "total": len(counts)
    })


@app.route("/api/courses", methods=["GET"])
def get_courses():
    """
    Return courses, optionally filtered.
    Query params: term, dept, faculty, status, room, consent, limit, offset
    """
    df = df_global.copy()

    # ── filters ──────────────────────────────────────────────────────────────
    term    = request.args.get("term")
    dept    = request.args.get("dept")
    faculty = request.args.get("faculty")
    status  = request.args.get("status")
    room    = request.args.get("room")
    consent = request.args.get("consent")

    if term:
        df = df[df["term"].str.lower() == term.lower()]
    if dept:
        df = df[df["sec_depts"].str.lower().str.contains(dept.lower(), na=False)]
    if faculty:
        df = df[df["faculty_name"].str.lower().str.contains(faculty.lower(), na=False)]
    if status:
        df = df[df["sect_status"].str.lower() == status.lower()]
    if room:
        df = df[df["sec_meeting_room"].str.lower().str.contains(room.lower(), na=False)]
    if consent:
        df = df[df["sec_faculty_consent_flag"].str.upper() == consent.upper()]

    # ── pagination ────────────────────────────────────────────────────────────
    total = len(df)
    try:
        offset = int(request.args.get("offset", 0))
        limit  = int(request.args.get("limit", total))
    except ValueError:
        abort(400, "limit and offset must be integers")

    page = df.iloc[offset : offset + limit]

    return jsonify({
        "total": total,
        "offset": offset,
        "limit": limit,
        "courses": df_to_records(page)
    })


@app.route("/api/courses/<path:sec_name>", methods=["GET"])
def get_course(sec_name: str):
    """Return a single course section by its section name (e.g. CS*330*A)."""
    course = find_course(sec_name)
    if course is None:
        abort(404, f"Course '{sec_name}' not found")
    return jsonify(course.to_dict())


@app.route("/api/courses", methods=["POST"])
def create_course():
    """Add a new course section. Body must be JSON."""
    global df_global
    data = request.get_json(force=True, silent=True)
    if not data:
        abort(400, "Request body must be valid JSON")

    required = ["term", "sec_name", "sec_short_title"]
    missing = [f for f in required if f not in data]
    if missing:
        abort(400, f"Missing required fields: {missing}")

    if find_course(data["sec_name"]) is not None:
        abort(409, f"Course '{data['sec_name']}' already exists. Use PUT to update.")

    new_row = {col: data.get(col) for col in df_global.columns}
    df_global = pd.concat([df_global, pd.DataFrame([new_row])], ignore_index=True)

    return jsonify({"message": "Course created", "course": new_row}), 201


@app.route("/api/courses/<path:sec_name>", methods=["PUT"])
def update_course(sec_name: str):
    """Update fields on an existing course section."""
    global df_global
    data = request.get_json(force=True, silent=True)
    if not data:
        abort(400, "Request body must be valid JSON")

    mask = df_global["sec_name"].str.lower() == sec_name.lower()
    if not mask.any():
        abort(404, f"Course '{sec_name}' not found")

    for key, value in data.items():
        if key in df_global.columns:
            df_global.loc[mask, key] = value

    updated = df_global[mask].iloc[0].to_dict()
    return jsonify({"message": "Course updated", "course": updated})


@app.route("/api/courses/<path:sec_name>", methods=["DELETE"])
def delete_course(sec_name: str):
    """Delete a course section by section name."""
    global df_global
    mask = df_global["sec_name"].str.lower() == sec_name.lower()
    if not mask.any():
        abort(404, f"Course '{sec_name}' not found")

    df_global = df_global[~mask].reset_index(drop=True)
    return jsonify({"message": f"Course '{sec_name}' deleted successfully"})


# ── Error Handlers ────────────────────────────────────────────────────────────

@app.errorhandler(400)
def bad_request(e):
    return jsonify({"error": "Bad Request", "message": str(e)}), 400

@app.errorhandler(404)
def not_found(e):
    return jsonify({"error": "Not Found", "message": str(e)}), 404

@app.errorhandler(409)
def conflict(e):
    return jsonify({"error": "Conflict", "message": str(e)}), 409

@app.errorhandler(500)
def server_error(e):
    return jsonify({"error": "Internal Server Error", "message": str(e)}), 500


# ── Entry Point ───────────────────────────────────────────────────────────────

if __name__ == "__main__":
    app.run(debug=True, port=5000)
