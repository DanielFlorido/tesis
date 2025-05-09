import whisper
import sys
import io
import os
import warnings

warnings.filterwarnings("ignore")  # Ocultar todos los warnings

# Asegúrate de que stdout use UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")

if len(sys.argv) < 2:
    print("Uso: python transcribir.py <ruta_del_audio>")
    sys.exit(1)

# Obtener la ruta del argumento
audio_path = sys.argv[1]

# Verificar si el archivo existe
if not os.path.isfile(audio_path):
    print(f"Error: El archivo '{audio_path}' no existe.")
    sys.exit(1)

# Cargar el modelo Whisper (puede ser "tiny", "base", "small", "medium", "large")
model = whisper.load_model("medium")

# Transcribir el audio especificando el idioma español
result = model.transcribe(audio_path, language="es", fp16=False)

# Imprimir el texto reconocido
# print("Recognized text:")
print(result["text"])
