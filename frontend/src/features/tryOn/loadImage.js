/**
 * Carga una URL y la deja lista en un canvas, no en un <img>.
 *
 * Por qué: si se le pasa el <img> directo a MediaPipe para detectar Y también se usa para
 * dibujar, cada uno puede interpretar la orientación EXIF de la foto (muy común en fotos de
 * celular) de forma distinta — el resultado es que los landmarks quedan calculados sobre una
 * versión de la imagen distinta a la que se ve en el canvas, y el marco termina posicionado mal.
 * Decodificando una sola vez a un canvas, tanto el detector como el dibujo trabajan sobre
 * exactamente los mismos píxeles.
 */
export function loadImage(source) {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = image.naturalWidth
      canvas.height = image.naturalHeight
      canvas.getContext('2d').drawImage(image, 0, 0)
      resolve(canvas)
    }
    image.onerror = () => reject(new Error('No se pudo cargar la imagen'))
    image.src = source
  })
}
