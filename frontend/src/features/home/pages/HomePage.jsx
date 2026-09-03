import { Link } from 'react-router-dom'
import { Layout } from '../../../components/Layout'
import { useAuth } from '../../../context/useAuth'

function HomeCard({ to, title, description }) {
  return (
    <Link
      to={to}
      className="rounded-lg border border-slate-200 bg-white p-5 transition hover:border-sky-300 hover:shadow-sm"
    >
      <h3 className="font-semibold text-slate-900">{title}</h3>
      <p className="mt-1 text-sm text-slate-500">{description}</p>
    </Link>
  )
}

const STAFF_CARDS = [
  {
    to: '/applicants',
    title: 'Solicitantes',
    description: 'Alta, edición y listado de beneficiarios con su DNI y datos de contacto.',
  },
  {
    to: '/donors',
    title: 'Donantes',
    description: 'Registro de quién donó y carga de los marcos que trajo cada donación.',
  },
  {
    to: '/frames',
    title: 'Inventario',
    description: 'Todos los marcos con su precinto, medidas y estado dentro del circuito.',
  },
  {
    to: '/assignments',
    title: 'Asignaciones',
    description: 'El circuito de cada par: envío a la óptica, retorno con los cristales y entrega.',
  },
  {
    to: '/appointments',
    title: 'Turnos',
    description: 'La agenda de atención del día: asistencia, reprogramaciones y cancelaciones.',
  },
  {
    to: '/shipments',
    title: 'Envíos',
    description: 'Paquetes de marcos entre sucursales, con el seguimiento que informa el correo.',
  },
  {
    to: '/try-on',
    title: 'Probador virtual',
    description: 'Probá sobre una foto cómo le quedan al beneficiario los marcos disponibles.',
  },
]

const APPLICANT_CARDS = [
  {
    to: '/my-appointments',
    title: 'Pedir un turno',
    description: 'Agendá un turno de atención, o de retiro si ya tenés un par de anteojos listo.',
  },
  {
    to: '/my-status',
    title: 'Estado de mi marco',
    description: 'Seguí el recorrido de tu marco: elegido, en la óptica, listo o entregado.',
  },
  {
    to: '/try-on',
    title: 'Probador virtual',
    description: 'Probate cómo te quedan los marcos disponibles antes de venir a la fundación.',
  },
]

export function HomePage() {
  const { role } = useAuth()
  const isApplicant = role === 'APPLICANT'
  const cards = isApplicant ? APPLICANT_CARDS : STAFF_CARDS

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Bienvenido</h2>
      <p className="mt-2 text-slate-600">
        {isApplicant
          ? 'Desde acá podés pedir un turno, ver el estado de tu marco y probarte los anteojos disponibles.'
          : 'Desde acá vas a poder gestionar solicitantes, donaciones, inventario y turnos. Los módulos se irán habilitando a medida que estén disponibles.'}
      </p>

      <div className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {cards.map((card) => (
          <HomeCard key={card.to} {...card} />
        ))}
      </div>
    </Layout>
  )
}
