const ALLIES = [
  {
    name: 'Óptica Solmar',
    role: 'Taller de Calibrado Óptico',
    logo: '/images/allies/solmar.webp',
    link: 'https://1764801.site123.me/',
    description: 'Laboratorio oftálmico encargado de la confección, calibración y montaje de cristales de graduación.',
  },
  {
    name: 'Óptica Uriburu',
    role: 'Sede y Recepción en CABA',
    logo: '/images/allies/uriburu.png',
    link: 'https://opticauriburu.com.ar/',
    description: 'Punto oficial de recepción de marcos donados en la Ciudad de Buenos Aires (Uriburu 56).',
  },
  {
    name: 'Vía Cargo',
    role: 'Logística Nacional Solidaria',
    logo: '/images/allies/via_cargo.png',
    link: 'https://www.viacargo.com.ar/',
    description: 'Transporte sin cargo de encomiendas de anteojos donados desde sucursales de todo el país a Tucumán.',
  },
  {
    name: 'Alartec',
    role: 'Seguridad Electrónica',
    logo: '/images/allies/alartec.jpg',
    description: 'Infraestructura tecnológica y de seguridad física para la sede central de Chacabuco 27.',
  },
  {
    name: 'Alegra',
    role: 'Software de Gestión Contable',
    logo: '/images/allies/alegra.png',
    link: 'https://www.alegra.com/',
    description: 'Plataforma en la nube para la gestión y administración contable transparente de la fundación.',
  },
  {
    name: 'Bach Propiedades',
    role: 'Apoyo Institucional',
    logo: '/images/allies/bach_propiedades.jpg',
    link: 'https://www.facebook.com/bachpropiedades',
    description: 'Compromiso continuo con la responsabilidad social empresarial y el desarrollo comunitario.',
  },
  {
    name: 'Video Factory',
    role: 'Producción Audiovisual',
    logo: '/images/allies/video_factory.png',
    description: 'Cobertura audiovisual profesional de entregas, testimonios y difusión de campañas solidarias.',
  },
]

export function AlliesSection() {
  return (
    <section id="alianzas" className="border-t border-slate-200 bg-white py-16 sm:py-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl text-center">
          <span className="text-xs font-black uppercase tracking-widest text-orange-600">
            Alianzas Estratégicas
          </span>
          <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-900 sm:text-3xl lg:text-4xl/tight">
            Este proyecto es posible gracias a todos los que donan los marcos de anteojos y a la alianza con estas empresas que ayudan a llevarlo a cabo:
          </h2>
          <p className="mt-4 text-sm leading-relaxed text-slate-600 sm:text-base">
            La sinergia entre la sociedad civil y el sector privado hace que la salud visual sea una realidad accesible y sustentable.
          </p>
        </div>

        <div className="mt-14 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {ALLIES.map((ally) => {
            const CardWrapper = ally.link ? 'a' : 'div'
            const extraProps = ally.link
              ? {
                  href: ally.link,
                  target: '_blank',
                  rel: 'noopener noreferrer',
                }
              : {}

            return (
              <CardWrapper
                key={ally.name}
                {...extraProps}
                className="group flex flex-col justify-between rounded-3xl border border-slate-200 bg-slate-50/60 p-6 text-center transition-all duration-300 hover:-translate-y-1 hover:border-orange-300 hover:bg-white hover:shadow-lg"
              >
                <div>
                  <div className="mx-auto flex h-24 w-full items-center justify-center rounded-2xl bg-white p-4 shadow-2xs transition group-hover:shadow-xs">
                    <img
                      src={ally.logo}
                      alt={`Logo de ${ally.name}`}
                      className="max-h-16 max-w-full object-contain filter transition duration-300 group-hover:scale-105"
                      loading="lazy"
                    />
                  </div>

                  <h3 className="mt-5 text-base font-bold text-slate-900">
                    {ally.name}
                  </h3>
                  <span className="mt-0.5 inline-block text-[11px] font-bold text-orange-600">
                    {ally.role}
                  </span>
                  <p className="mt-2 text-xs leading-relaxed text-slate-600">
                    {ally.description}
                  </p>
                </div>

                {ally.link && (
                  <div className="mt-5 pt-3 border-t border-slate-200/60 flex items-center justify-center gap-1 text-[11px] font-bold text-orange-600 group-hover:text-orange-700">
                    <span>Visitar sitio web</span>
                    <svg className="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </div>
                )}
              </CardWrapper>
            )
          })}
        </div>

        {/* Banner para nuevas empresas interesadas */}
        <div className="mt-14 overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900 to-slate-950 p-8 text-white sm:p-10 shadow-xl">
          <div className="flex flex-col items-center justify-between gap-6 md:flex-row text-center md:text-left">
            <div>
              <span className="text-[11px] font-bold uppercase tracking-wider text-orange-400">
                Responsabilidad Social Empresaria (RSE)
              </span>
              <h3 className="mt-1 text-xl font-black text-white sm:text-2xl">
                ¿Tu empresa quiere sumarse como aliada?
              </h3>
              <p className="mt-2 text-xs sm:text-sm text-slate-300 max-w-2xl leading-relaxed">
                Podés colaborar instalando una urna de recolección en tus sucursales, donando insumos, apoyando los gastos operativos o brindando difusión.
              </p>
            </div>
            <a
              href="https://wa.me/5493813982020?text=Hola,%20me%20comunico%20en%20nombre%20de%20una%20empresa%20para%20sumarnos%20al%20Banco%20de%20Anteojos"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex shrink-0 items-center justify-center rounded-xl bg-orange-600 px-6 py-3.5 text-xs font-bold text-white shadow-md hover:bg-orange-700 transition active:scale-95"
            >
              Contactar al equipo de alianzas
            </a>
          </div>
        </div>
      </div>
    </section>
  )
}
