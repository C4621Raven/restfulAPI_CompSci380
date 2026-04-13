# Schedule API

A RESTful API built with Python and Flask that serves course schedule data from an Excel spreadsheet (`Final_Draft_of_Schedule.xlsx`).

---

## Requirements

- Python 3.11+
- pip packages: `flask`, `pandas`, `openpyxl`
- OR Docker (no Python install needed)

---

## Running the API

### Option 1 — Run with Python directly

**1. Install dependencies:**
```
pip install flask pandas openpyxl
```

**2. Start the server:**
```
python schedule_api.py
```

**3. The API will be available at:**
```
http://localhost:5000/api/
```

---

### Option 2 — Run with Docker

This is the recommended way to run the API as it requires no Python installation on your machine.

#### What you need in your project folder:
```
📁 your-folder/
├── Dockerfile
├── schedule_api.py
├── requirements.txt
└── Final_Draft_of_Schedule.xlsx
```

#### Dockerfile contents:
```dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY schedule_api.py .
COPY Final_Draft_of_Schedule.xlsx .

EXPOSE 5000

CMD ["python", "schedule_api.py"]
```

> **Important:** The file must be named exactly `Dockerfile` with no file extension. On Windows, make sure File Explorer is showing file extensions (View → File name extensions) so you can confirm it is not saved as `Dockerfile.txt`.

#### requirements.txt contents:
```
flask>=3.0.0
pandas>=2.0.0
openpyxl>=3.1.0
```

#### Bottom of schedule_api.py must be:
```python
if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=False, port=5000)
```

> **Important:** The host must be `0.0.0.0` and not `localhost` or a specific IP address, otherwise the container will fail to start.

---

#### Step by step — Running the API in Docker:

**Step 1 — Open a terminal in your project folder:**

On Windows, open the folder in File Explorer, click the address bar, type `cmd` and press Enter.

**Step 2 — Build the Docker image:**
```
docker build -t schedule-api .
```
Don't forget the `.` at the end. This tells Docker to look for the Dockerfile in the current folder. This step downloads Python and installs all dependencies inside the image.

**Step 3 — Run the container:**
```
docker run -p 5000:5000 schedule-api
```
The `-p 5000:5000` maps port 5000 on your machine to port 5000 inside the container.

You should see:
```
* Serving Flask app 'schedule_api'
* Debug mode: off
* Running on all addresses (0.0.0.0)
* Running on http://127.0.0.1:5000
```

**Step 4 — Test it in your browser:**
```
http://localhost:5000/api/terms
```

**Step 5 — Stop the container when done:**
```
docker stop $(docker ps -q)
```
Or on Windows Command Prompt:
```
for /f "tokens=*" %i in ('docker ps -q') do docker stop %i
```

---

#### Useful Docker commands:

| Command | What it does |
|---------|-------------|
| `docker ps` | See all running containers |
| `docker ps -a` | See all containers including stopped ones |
| `docker logs <container_id>` | See API output and errors |
| `docker stop <container_id>` | Stop a running container |
| `docker restart <container_id>` | Restart a container |
| `docker rm <container_id>` | Remove a stopped container |
| `docker rmi schedule-api` | Remove the image |
| `docker build --no-cache -t schedule-api .` | Force a completely fresh build |

---

#### Troubleshooting:

**Cannot assign requested address**
The `host` in `schedule_api.py` is not set to `0.0.0.0`. Fix the last line and rebuild:
```
docker build --no-cache -t schedule-api .
docker run -p 5000:5000 schedule-api
```

**No such file or directory: Dockerfile**
The Dockerfile has a `.txt` extension. Rename it by running:
```
ren Dockerfile.txt Dockerfile
```

**Old changes not showing up**
Docker may be using a cached image. Force a fresh build:
```
docker build --no-cache -t schedule-api .
```

**Port 5000 already in use**
Something else is using port 5000. Run on a different port:
```
docker run -p 5001:5000 schedule-api
```
Then access the API at `http://localhost:5001/api/terms`.

---

### Option 3 — Run with Docker Compose (API + Java client together)

```
docker-compose up --build
```

---

## Endpoints

### GET /api/terms
Returns all available terms and how many courses are in each.

**URL:**
```
GET http://localhost:5000/api/terms
```

**Example Response:**
```json
{
  "total": 2,
  "terms": [
    { "term": "26FA", "course_count": 449 },
    { "term": "26SU", "course_count": 26  }
  ]
}
```

---

### GET /api/courses
Returns all courses. Supports optional filters as query parameters.

**URL:**
```
GET http://localhost:5000/api/courses
```

**Query Parameters (all optional):**

| Parameter | Description | Example |
|-----------|-------------|---------|
| `term` | Filter by term | `26FA` or `26SU` |
| `dept` | Filter by department | `MACS`, `ECBA` |
| `faculty` | Filter by faculty name (partial match) | `Smith` |
| `status` | Filter by section status | `A` |
| `room` | Filter by meeting room (partial match) | `HC` |
| `consent` | Faculty consent required | `Y` or `N` |
| `limit` | Max number of results to return | `10` |
| `offset` | Number of results to skip (for pagination) | `20` |

**Example URLs:**
```
GET http://localhost:5000/api/courses
GET http://localhost:5000/api/courses?term=26FA
GET http://localhost:5000/api/courses?term=26FA&dept=MACS
GET http://localhost:5000/api/courses?faculty=Smith
GET http://localhost:5000/api/courses?term=26FA&limit=10&offset=0
GET http://localhost:5000/api/courses?consent=Y
```

**Example Response:**
```json
{
  "total": 449,
  "offset": 0,
  "limit": 449,
  "courses": [
    {
      "term": "26FA",
      "sec_depts": "ECBA",
      "sec_name": "ACCT*261*A",
      "sec_short_title": "Financial Accounting",
      "sect_status": "A",
      "faculty_name": "Staff",
      "instr_methods": "LEC",
      "sec_faculty_consent_flag": "N",
      "course_limit": 25,
      "sec_meeting_room": "AB 233",
      "meeting_times": "MWF 09:00AM 09:50AM",
      "sec_course_types": "FRYR",
      "xsec_reqs_printed_comments": null
    }
  ]
}
```

---

### GET /api/courses/{sec_name}
Returns a single course by its section name.

**URL:**
```
GET http://localhost:5000/api/courses/CS*330*A
```

**Example Response:**
```json
{
  "term": "26SU",
  "sec_depts": "MACS",
  "sec_name": "CS*330*A",
  "sec_short_title": "Database Systems",
  "sect_status": "A",
  "faculty_name": "Amon Seagull",
  "instr_methods": "LEC",
  "sec_faculty_consent_flag": "N",
  "course_limit": 15,
  "sec_meeting_room": "ONLY RMOTE",
  "meeting_times": "MTTH  10:30AM 12:00PM",
  "sec_course_types": null,
  "xsec_reqs_printed_comments": "Note: Full Session PREQ: CS*211 (Grade of C or higher) or instructor permission"
}
```

**Error Response (course not found):**
```json
{
  "error": "Not Found",
  "message": "Course 'CS*330*A' not found"
}
```

---

### POST /api/courses
Creates a new course section.

**URL:**
```
POST http://localhost:5000/api/courses
```

**Headers:**
```
Content-Type: application/json
```

**Required Fields:**

| Field | Description |
|-------|-------------|
| `term` | Term code (e.g. `26FA`) |
| `sec_name` | Section name (e.g. `CS*101*A`) |
| `sec_short_title` | Course title |

**Example Request Body:**
```json
{
  "term": "26FA",
  "sec_name": "CS*101*A",
  "sec_short_title": "Intro to Computer Science",
  "sec_depts": "MACS",
  "faculty_name": "Jane Doe",
  "instr_methods": "LEC",
  "sect_status": "A",
  "course_limit": 30,
  "sec_meeting_room": "HC 101",
  "meeting_times": "MWF 09:00AM 09:50AM",
  "sec_faculty_consent_flag": "N"
}
```

**Example Response (201 Created):**
```json
{
  "message": "Course created",
  "course": {
    "term": "26FA",
    "sec_name": "CS*101*A",
    "sec_short_title": "Intro to Computer Science",
    ...
  }
}
```

**Error Response (course already exists):**
```json
{
  "error": "Conflict",
  "message": "Course 'CS*101*A' already exists. Use PUT to update."
}
```

---

### PUT /api/courses/{sec_name}
Updates fields on an existing course. Only include fields you want to change.

**URL:**
```
PUT http://localhost:5000/api/courses/CS*101*A
```

**Headers:**
```
Content-Type: application/json
```

**Example Request Body:**
```json
{
  "faculty_name": "John Smith",
  "sec_meeting_room": "HC 202"
}
```

**Example Response:**
```json
{
  "message": "Course updated",
  "course": {
    "term": "26FA",
    "sec_name": "CS*101*A",
    "faculty_name": "John Smith",
    "sec_meeting_room": "HC 202",
    ...
  }
}
```

---

### DELETE /api/courses/{sec_name}
Deletes a course section.

**URL:**
```
DELETE http://localhost:5000/api/courses/CS*101*A
```

**Example Response:**
```json
{
  "message": "Course 'CS*101*A' deleted successfully"
}
```

**Error Response (course not found):**
```json
{
  "error": "Not Found",
  "message": "Course 'CS*101*A' not found"
}
```

---

## Error Codes

| Code | Meaning |
|------|---------|
| `200` | Success |
| `201` | Created successfully |
| `400` | Bad request (missing or invalid fields) |
| `404` | Course or resource not found |
| `409` | Conflict (course already exists) |
| `500` | Internal server error |

---

## Course Fields Reference

| Field | Description | Example |
|-------|-------------|---------|
| `term` | Term code | `26FA`, `26SU` |
| `sec_depts` | Department code | `MACS`, `ECBA` |
| `sec_name` | Section name | `CS*330*A` |
| `sec_short_title` | Course title | `Database Systems` |
| `sect_status` | Section status | `A` (Active) |
| `faculty_name` | Instructor name | `Amon Seagull` |
| `instr_methods` | Instruction method | `LEC`, `LAB` |
| `sec_faculty_consent_flag` | Consent required | `Y` or `N` |
| `course_limit` | Max enrollment | `25` |
| `sec_meeting_room` | Room location | `HC 212` |
| `meeting_times` | Days and times | `MWF 09:00AM 09:50AM` |
| `sec_course_types` | Course type flags | `FRYR`, `HU` |
| `xsec_reqs_printed_comments` | Prerequisites / notes | `PREQ: CS*211` |

---

## Accessing from Another Computer

If the API is running on a different machine, replace `localhost` with the server's IP address:

```
http://192.168.1.50:5000/api/terms
```

To find the server's IP address run `ipconfig` on Windows and look for the **Wireless LAN IPv4 Address**. Both computers must be on the same network.
