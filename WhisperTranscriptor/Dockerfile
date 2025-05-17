FROM python:3.10-slim

RUN apt-get update && apt-get install -y ffmpeg git

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY Transcriptor.py .

EXPOSE 8000
CMD ["uvicorn", "Transcriptor:app", "--host", "0.0.0.0", "--port", "8000"]