const AWARDS = [
  {
    title: 'Premio Abanderados de la Argentina Solidaria',
    institution: 'Premio Abanderados',
    image: '/images/awards/abanderados.png',
    description:
      'Reconocimiento nacional anual que destaca a aquellos argentinos que inspiran por su compromiso y dedicación desinteresada hacia los demás.',
    link: 'https://www.premioabanderados.com.ar/abanderados/enrique-bach',
    linkLabel: 'Conocer más',
  },
  {
    title: 'Premio BritCham a la Excelencia en Sostenibilidad',
    institution: 'Cámara de Comercio Argentino-Británica',
    image: '/images/awards/britcham.png',
    description:
      'Distinción en la categoría de inclusión social y sustentabilidad ambiental por el circuito circular de reciclado de marcos.',
    link: 'https://premiosostenibilidad.com.ar/ganadores-2022/',
    linkLabel: 'Ver ganadores',
  },
  {
    title: 'Premio Mentes Transformadoras',
    institution: 'Innovación Social',
    image: '/images/awards/mentes_transformadoras.jpg',
    description:
      'Galardonado como mejor proyecto de innovación social por su metodología colaborativa, accesible y transparente.',
    link: 'https://www.facebook.com/mentestransformadorasok/',
    linkLabel: 'Ver proyecto',
  },
  {
    title: 'Ciudadano Ilustre de Yerba Buena',
    institution: 'Municipalidad de Yerba Buena',
    image: '/images/awards/yerba_buena.webp',
    description:
      'Distinción honorífica otorgada a Enrique Bach por el Honorable Concejo Deliberante en mérito a su labor social comunitaria.',
  },
  {
    title: 'Declarado de Interés Municipal',
    institution: 'San Miguel de Tucumán',
    image: '/images/awards/smt.jpg',
    description:
      'Reconocimiento institucional del Concejo Deliberante de la Municipalidad de San Miguel de Tucumán al impacto del Banco de Anteojos.',
  },
  {
    title: 'Distinción San Francisco de Asís',
    institution: 'Honorable Legislatura de Tucumán',
    image: '/images/awards/legislatura.png',
    description:
      'Máxima distinción comunitaria otorgada por la Legislatura de la Provincia de Tucumán al compromiso social y ambiental.',
  },
  {
    title: 'Mención Solidarios en Red Tucumán',
    institution: 'Red de Organizaciones de la Sociedad Civil',
    image: '/images/awards/solidarios_red.png',
    description:
      'Reconocimiento como miembro activo y articulador de soluciones solidarias dentro de la red provincial de ONGs de Tucumán.',
    link: 'https://www.servoluntario.org/redsolidaria/',
    linkLabel: 'Conocer la red',
  },
]

export function AwardsSection() {
  return (
    <section id="premios" className="border-t border-slate-200 bg-slate-50 py-16 sm:py-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl text-center">
          <span className="text-xs font-black uppercase tracking-widest text-orange-600">
            Reconocimiento Institucional
          </span>
          <h2 className="mt-2 text-2xl font-black tracking-tight text-slate-900 sm:text-4xl">
            Premios y Menciones
          </h2>
          <p className="mt-4 text-sm leading-relaxed text-slate-600 sm:text-base">
            El aval de organismos públicos, cámaras empresariales y redes de la sociedad civil que certifican la seriedad, transparencia e impacto de nuestro trabajo.
          </p>
        </div>

        <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {AWARDS.map((award) => (
            <div
              key={award.title}
              className="group flex flex-col justify-between rounded-3xl border border-slate-200 bg-white p-6 shadow-xs transition hover:border-orange-300 hover:shadow-md"
            >
              <div>
                <div className="flex h-20 items-center justify-center rounded-2xl bg-slate-50 p-3">
                  <img
                    src={award.image}
                    alt={award.title}
                    className="max-h-16 max-w-full object-contain filter transition duration-300 group-hover:scale-105"
                  />
                </div>

                <div className="mt-5">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-orange-600">
                    {award.institution}
                  </span>
                  <h3 className="mt-1 text-sm font-bold leading-snug text-slate-900">
                    {award.title}
                  </h3>
                  <p className="mt-2 text-xs leading-relaxed text-slate-600">
                    {award.description}
                  </p>
                </div>
              </div>

              {award.link && (
                <div className="mt-5 pt-4 border-t border-slate-100">
                  <a
                    href={award.link}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1.5 text-xs font-bold text-orange-600 hover:text-orange-700 transition"
                  >
                    <span>{award.linkLabel || 'Conocer más'}</span>
                    <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
