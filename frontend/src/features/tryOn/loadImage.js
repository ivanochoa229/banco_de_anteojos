/** Carga una URL en un HTMLImageElement ya decodificado, listo para dibujar en el canvas. */
export function loadImage(source) {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = () => reject(new Error('No se pudo cargar la imagen'))
    image.src = source
  })
}
