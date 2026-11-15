"""
Genera las imágenes y PDFs de la demo a partir de web/src/main/resources/demo/datos-demo.json.

- Portadas (400x600 JPG) con degradado, título y autor.
- Avatares (300x300 JPG) con las iniciales del usuario.
- Fragmentos en PDF con texto real (generador mínimo, sin dependencias externas).

Todo el contenido es original del proyecto, así que no hay problemas de derechos.

Requisitos: Python 3 y Pillow (pip install pillow). Las fuentes se toman de Windows
(C:/Windows/Fonts); en otros sistemas se usa la fuente por defecto de Pillow.

Uso (desde la raíz del proyecto):
    python scripts/generar_recursos_demo.py
"""
import hashlib
import json
import os
import sys

from PIL import Image, ImageDraw, ImageFont

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATOS = os.path.join(RAIZ, "web", "src", "main", "resources", "demo", "datos-demo.json")
DIR_IMG = os.path.join(RAIZ, "web", "src", "main", "webapp", "resources", "img")
DIR_PDF = os.path.join(RAIZ, "web", "src", "main", "webapp", "resources", "pdf")
DIR_FUENTES = "C:/Windows/Fonts"


def fuente(nombre, tam):
    ruta = os.path.join(DIR_FUENTES, nombre)
    if os.path.exists(ruta):
        return ImageFont.truetype(ruta, tam)
    return ImageFont.load_default(size=tam)


def hex_a_rgb(color):
    color = color.lstrip("#")
    return tuple(int(color[i:i + 2], 16) for i in (0, 2, 4))


def luminancia(rgb):
    return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]


def envolver(texto, fnt, ancho_max, medir):
    lineas, actual = [], ""
    for palabra in texto.split():
        prueba = (actual + " " + palabra).strip()
        if medir(prueba, fnt) <= ancho_max:
            actual = prueba
        else:
            if actual:
                lineas.append(actual)
            actual = palabra
    if actual:
        lineas.append(actual)
    return lineas


# ---------------------------------------------------------------- imágenes

def generar_portada(obra, autores, ruta):
    ancho, alto = 400, 600
    c1, c2 = hex_a_rgb(obra["colores"][0]), hex_a_rgb(obra["colores"][1])
    img = Image.new("RGB", (ancho, alto))
    dib = ImageDraw.Draw(img)
    for y in range(alto):
        t = y / (alto - 1)
        dib.line([(0, y), (ancho, y)], fill=tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3)))

    # Elemento decorativo: un círculo translúcido cuya posición depende del título.
    semilla = int(hashlib.md5(obra["clave"].encode()).hexdigest(), 16)
    capa = Image.new("RGBA", (ancho, alto), (0, 0, 0, 0))
    dc = ImageDraw.Draw(capa)
    cx, cy, r = 80 + semilla % 240, 260 + (semilla >> 8) % 120, 90 + (semilla >> 16) % 60
    dc.ellipse([cx - r, cy - r, cx + r, cy + r], fill=c2 + (70,))
    dc.ellipse([cx - r // 2, cy - r // 2, cx + r // 2, cy + r // 2], fill=c1 + (60,))
    img = Image.alpha_composite(img.convert("RGBA"), capa).convert("RGB")
    dib = ImageDraw.Draw(img)

    claro = (250, 247, 240)
    oscuro = (30, 30, 30)
    color_titulo = claro if luminancia(c1) < 140 else oscuro
    color_autor = claro if luminancia(c2) < 140 else oscuro

    def medir(t, f):
        return dib.textlength(t, font=f)

    f_titulo = fuente("georgiab.ttf", 38)
    y = 60
    for linea in envolver(obra["titulo"].upper(), f_titulo, ancho - 60, medir):
        dib.text(((ancho - medir(linea, f_titulo)) / 2, y), linea, font=f_titulo, fill=color_titulo)
        y += 48
    dib.line([(ancho / 2 - 40, y + 12), (ancho / 2 + 40, y + 12)], fill=color_titulo, width=2)

    f_autor = fuente("georgia.ttf", 22)
    nombre_autor = ", ".join(autores)
    dib.text(((ancho - medir(nombre_autor, f_autor)) / 2, alto - 70), nombre_autor, font=f_autor, fill=color_autor)
    img.save(ruta, "JPEG", quality=88)


def generar_avatar(nombre, indice, ruta):
    tam = 300
    paleta = ["#2f4f3a", "#7a3b4f", "#0f1e3d", "#3e2723", "#1d4e5f", "#4a3f35", "#5b4b8a", "#8a4b2f"]
    fondo = hex_a_rgb(paleta[indice % len(paleta)])
    img = Image.new("RGB", (tam, tam), fondo)
    dib = ImageDraw.Draw(img)
    iniciales = "".join(p[0] for p in nombre.split()[:2]).upper()
    fnt = fuente("segoeuib.ttf", 120)
    caja = dib.textbbox((0, 0), iniciales, font=fnt)
    x = (tam - (caja[2] - caja[0])) / 2 - caja[0]
    y = (tam - (caja[3] - caja[1])) / 2 - caja[1]
    dib.text((x, y), iniciales, font=fnt, fill=(250, 247, 240))
    img.save(ruta, "JPEG", quality=90)


# ---------------------------------------------------------------- PDF

class PdfSimple:
    """Generador mínimo de PDF de texto con las fuentes estándar Times (WinAnsiEncoding)."""

    ANCHO, ALTO = 595, 842  # A4 en puntos
    MARGEN = 72

    def __init__(self):
        self.paginas = []
        self.actual = None
        self.y = 0
        self._medidores = {
            "F1": fuente("times.ttf", 100),
            "F2": fuente("timesbd.ttf", 100),
            "F3": fuente("timesi.ttf", 100) if os.path.exists(os.path.join(DIR_FUENTES, "timesi.ttf")) else fuente("times.ttf", 100),
        }
        self.nueva_pagina()

    def medir(self, texto, f):
        nombre, tam = f
        return self._medidores[nombre].getlength(texto) * tam / 100

    def nueva_pagina(self):
        self.actual = []
        self.paginas.append(self.actual)
        self.y = self.ALTO - self.MARGEN

    @staticmethod
    def _escapar(texto):
        datos = texto.encode("cp1252", errors="replace").decode("latin-1")
        return datos.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")

    def linea(self, texto, f, interlineado, centrado=False):
        if self.y - interlineado < self.MARGEN + 30:
            self.nueva_pagina()
        self.y -= interlineado
        x = (self.ANCHO - self.medir(texto, f)) / 2 if centrado else self.MARGEN
        self.actual.append(f"BT /{f[0]} {f[1]} Tf {x:.2f} {self.y:.2f} Td ({self._escapar(texto)}) Tj ET")

    def parrafo(self, texto, f, interlineado, sangria=True):
        ancho = self.ANCHO - 2 * self.MARGEN
        lineas = envolver(("    " if sangria else "") + texto, f, ancho, self.medir)
        for linea in lineas:
            self.linea(linea, f, interlineado)
        self.y -= interlineado * 0.5

    def guardar(self, ruta, pie):
        objetos = []

        def agregar(contenido):
            objetos.append(contenido)
            return len(objetos)

        catalogo = agregar(None)
        paginas_id = agregar(None)
        fuentes = {
            "F1": agregar("<< /Type /Font /Subtype /Type1 /BaseFont /Times-Roman /Encoding /WinAnsiEncoding >>"),
            "F2": agregar("<< /Type /Font /Subtype /Type1 /BaseFont /Times-Bold /Encoding /WinAnsiEncoding >>"),
            "F3": agregar("<< /Type /Font /Subtype /Type1 /BaseFont /Times-Italic /Encoding /WinAnsiEncoding >>"),
        }
        recursos = "<< /Font << " + " ".join(f"/{k} {v} 0 R" for k, v in fuentes.items()) + " >> >>"
        hijos = []
        total = len(self.paginas)
        for n, comandos in enumerate(self.paginas, start=1):
            texto_pie = f"{pie}  -  {n}/{total}"
            x_pie = (self.ANCHO - self.medir(texto_pie, ("F3", 9))) / 2
            flujo = "\n".join(comandos + [f"BT /F3 9 Tf {x_pie:.2f} 40 Td ({self._escapar(texto_pie)}) Tj ET"])
            datos = flujo.encode("latin-1")
            contenido = agregar(f"<< /Length {len(datos)} >>\nstream\n".encode("latin-1") + datos + b"\nendstream")
            hijos.append(agregar(
                f"<< /Type /Page /Parent {paginas_id} 0 R /MediaBox [0 0 {self.ANCHO} {self.ALTO}] "
                f"/Resources {recursos} /Contents {contenido} 0 R >>"))
        objetos[catalogo - 1] = f"<< /Type /Catalog /Pages {paginas_id} 0 R >>"
        objetos[paginas_id - 1] = f"<< /Type /Pages /Kids [{' '.join(f'{h} 0 R' for h in hijos)}] /Count {len(hijos)} >>"

        salida = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
        posiciones = []
        for i, obj in enumerate(objetos, start=1):
            posiciones.append(len(salida))
            cuerpo = obj if isinstance(obj, bytes) else obj.encode("latin-1")
            salida += f"{i} 0 obj\n".encode() + cuerpo + b"\nendobj\n"
        inicio_xref = len(salida)
        salida += f"xref\n0 {len(objetos) + 1}\n0000000000 65535 f \n".encode()
        for p in posiciones:
            salida += f"{p:010d} 00000 n \n".encode()
        salida += f"trailer\n<< /Size {len(objetos) + 1} /Root {catalogo} 0 R >>\nstartxref\n{inicio_xref}\n%%EOF\n".encode()
        with open(ruta, "wb") as archivo:
            archivo.write(salida)


def generar_fragmento(obra, autores, ruta):
    pdf = PdfSimple()
    pdf.y -= 60
    for linea in envolver(obra["titulo"], ("F2", 24), pdf.ANCHO - 2 * pdf.MARGEN, pdf.medir):
        pdf.linea(linea, ("F2", 24), 30, centrado=True)
    pdf.linea(", ".join(autores), ("F3", 14), 26, centrado=True)
    pdf.linea("Fragmento de muestra", ("F1", 11), 30, centrado=True)
    pdf.y -= 30
    for parrafo in obra["texto"]:
        pdf.parrafo(parrafo, ("F1", 12), 18)
    pdf.guardar(ruta, f"{obra['titulo']} - Plataforma Literaria Impulso")


# ---------------------------------------------------------------- principal

def main():
    with open(DATOS, encoding="utf-8") as archivo:
        datos = json.load(archivo)
    os.makedirs(DIR_IMG, exist_ok=True)
    os.makedirs(DIR_PDF, exist_ok=True)

    nombres = {e["clave"]: e["nombre"] for e in datos["escritores"]}
    generados = []
    for obra in datos["obras"]:
        autores = [nombres[a] for a in obra["autores"]]
        generar_portada(obra, autores, os.path.join(DIR_IMG, obra["portada"]))
        generar_fragmento(obra, autores, os.path.join(DIR_PDF, obra["fragmento"]))
        generados += [obra["portada"], obra["fragmento"]]

    for indice, usuario in enumerate(datos["escritores"] + datos["lectores"] + [datos["administrador"]]):
        generar_avatar(usuario["nombre"], indice, os.path.join(DIR_IMG, usuario["imagen"]))
        generados.append(usuario["imagen"])

    print(f"Se generaron {len(generados)} archivos.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
