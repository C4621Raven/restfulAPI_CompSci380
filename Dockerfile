FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY schedule_api.py .
COPY Final_Draft_of_Schedule.xlsx .

EXPOSE 5000

CMD ["python", "schedule_api.py"]