import cv2
from pyzbar.pyzbar import decode
import os
import json

# Make one method to decode the barcode
INICIAIS_ACEITAS = ["NU", "FL", "FN", "BR"]
PASTA = './pasta'


def rotate_image(mat, angle):
    """
    Rotates an image (angle in degrees) and expands image to avoid cropping
    """
    height, width = mat.shape[:2]  # image shape has 3 dimensions
    image_center = (
        width / 2,
        height / 2)  # getRotationMatrix2D needs coordinates in reverse order (width, height) compared to shape

    rotation_mat = cv2.getRotationMatrix2D(image_center, angle, 1.)

    # rotation calculates the cos and sin, taking absolutes of those.
    abs_cos = abs(rotation_mat[0, 0])
    abs_sin = abs(rotation_mat[0, 1])

    # find the new width and height bounds
    bound_w = int(height * abs_sin + width * abs_cos)
    bound_h = int(height * abs_cos + width * abs_sin)

    # subtract old image center (bringing image back to origo) and adding the new image center coordinates
    rotation_mat[0, 2] += bound_w / 2 - image_center[0]
    rotation_mat[1, 2] += bound_h / 2 - image_center[1]

    # rotate image with the new bounds and translated rotation matrix
    rotated_mat = cv2.warpAffine(mat, rotation_mat, (bound_w, bound_h))
    return rotated_mat


def BarcodeReader(image):
    lista = []
    for x in range(0, 360):
        img = cv2.imread(image, cv2.IMREAD_GRAYSCALE)
        img = rotate_image(img, x)
        detectedBarcodes = decode(img)
        if detectedBarcodes:
            barcode = detectedBarcodes[0].data
            split = str(barcode).split("'")[1]
            lista.append(split)
            if split[:2] in INICIAIS_ACEITAS:
                lista = [split]
                break
            if len(lista) >= 5:
                break
    maior_incidencia = 0
    codigoDeBarras = ""
    for elemento in set(lista):
        quantidade_elementos_lista = lista.count(elemento)
        if quantidade_elementos_lista > maior_incidencia:
            maior_incidencia = quantidade_elementos_lista
            codigoDeBarras = elemento
    return codigoDeBarras


if __name__ == "__main__":
    # Criando Dicionario para armazenar a imagem relacionada ao codigo de barras.
    dicionario_imagens_cod = dict()
    # Variavel para contabilizar os documentos.
    quantidade_documentos_registrados = 0
    # Identificar nomes pelos arquivos/fotos dentro da pastar.
    for diretorio, subpastas, arquivos in os.walk(PASTA):
        for arquivo in arquivos:
            data = BarcodeReader("pasta/" + arquivo)
            dicionario_imagens_cod[arquivo] = data
            print(dicionario_imagens_cod)
    # Gerando JSON
    with open('data1.json', 'w', encoding='utf-8') as outfile:
        json.dump(dicionario_imagens_cod, outfile, ensure_ascii=False, indent=2)
