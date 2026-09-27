import { useState } from 'react'

const TESTIMONIAL_VIDEOS = [
  {
    id: '787dL0c4jqA',
    title: 'Video Testimonio',
  },
  {
    id: 'CvYWOAn1jJ0',
    title: 'Video de Testimonio',
  },
  {
    id: 'aL6FdEX0G24',
    title: 'Video de Testimonio',
  },
  {
    id: '5IckljTZS8E',
    title: 'Video de Testimonios',
  },
  {
    id: 'FFeknv9PX2Y',
    title: 'Video de Testimonio',
  },
  {
    id: '-n3NjFmCdhM',
    title: 'Video de Testimonio',
  },
]

export function VideoTestimonialsSection() {
  const [currentIndex, setCurrentIndex] = useState(0)

  const currentVideo = TESTIMONIAL_VIDEOS[currentIndex]

  function handlePrev() {
    setCurrentIndex((prev) => (prev === 0 ? TESTIMONIAL_VIDEOS.length - 1 : prev - 1))
  }

  function handleNext() {
    setCurrentIndex((prev) => (prev === TESTIMONIAL_VIDEOS.length - 1 ? 0 : prev + 1))
  }

  return (
    <section id="testimonios" className="border-t border-slate-200 bg-white py-16 sm:py-20">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
            Testimonios
          </h2>
        </div>

        {/* Contenedor del video con flechas a los costados */}
        <div className="mx-auto mt-8 max-w-5xl">
          <div className="flex items-center justify-center gap-2 sm:gap-4 md:gap-6">
            {/* Flecha izquierda al costado */}
            <button
              type="button"
              onClick={handlePrev}
              className="flex h-9 w-9 sm:h-12 sm:w-12 shrink-0 items-center justify-center rounded-lg bg-orange-500 text-white shadow-md transition hover:bg-orange-600 active:scale-95"
              aria-label="Video anterior"
            >
              <svg className="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
              </svg>
            </button>

            {/* Video en el centro */}
            <div className="relative aspect-video w-full max-w-4xl overflow-hidden rounded-xl sm:rounded-2xl bg-black shadow-xl">
              <iframe
                key={currentVideo.id}
                src={`https://www.youtube.com/embed/${currentVideo.id}?rel=0`}
                title={currentVideo.title}
                className="absolute inset-0 h-full w-full"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowFullScreen
              />
            </div>

            {/* Flecha derecha al costado */}
            <button
              type="button"
              onClick={handleNext}
              className="flex h-9 w-9 sm:h-12 sm:w-12 shrink-0 items-center justify-center rounded-lg bg-orange-500 text-white shadow-md transition hover:bg-orange-600 active:scale-95"
              aria-label="Video siguiente"
            >
              <svg className="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>

          {/* Texto de aviso */}
          <p className="mt-4 text-center text-xs sm:text-sm text-slate-600">
            Para reproducir el siguiente, pausa el actual
          </p>
        </div>
      </div>
    </section>
  )
}
