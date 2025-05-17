from fastapi import FastAPI, File, UploadFile
import whisper
import os

app = FastAPI()
model = whisper.load_model("medium")  # Puedes cambiar el modelo si lo deseas

@app.post("/transcribe")
async def transcribe(audio: UploadFile = File(...)):
    temp_path = "temp_audio.mp3"
    with open(temp_path, "wb") as f:
        f.write(await audio.read())
    result = model.transcribe(temp_path, language="es", fp16=False)
    os.remove(temp_path)
    return {"text": result["text"]}