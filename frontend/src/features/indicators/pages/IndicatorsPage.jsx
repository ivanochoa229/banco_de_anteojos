import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { indicatorsApi } from '../../../api/indicatorsApi'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { Layout } from '../../../components/Layout'
import { IndicatorCard } from '../components/IndicatorCard'
import { MonthlyDeliveriesChart } from '../components/MonthlyDeliveriesChart'

function formatPercentage(rate) {
  return rate === null || rate === undefined ? '—' : `${Math.round(rate * 100)}%`
}

export function IndicatorsPage() {
  const [range, setRange] = useState({ from: '', to: '' })
  const [appliedRange, setAppliedRange] = useState({ from: '', to: '' })

  const indicatorsQuery = useQuery({
    queryKey: ['indicators', appliedRange],
    queryFn: () => indicatorsApi.get(appliedRange.from || undefined, appliedRange.to || undefined),
  })

  const rangeError =
    range.from && range.to && range.from > range.to
      ? 'La fecha desde no puede ser posterior a la fecha hasta'
      : null

  function handleSubmit(event) {
    event.preventDefault()
    if (rangeError) return
    setAppliedRange(range)
  }

  function handleClear() {
    setRange({ from: '', to: '' })
    setAppliedRange({ from: '', to: '' })
  }

  const data = indicatorsQuery.data

  return (
    <Layout>
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Panel de impacto</h2>
        <p className="mt-1 text-sm text-slate-500">
          Métricas agregadas de la fundación. Sin filtro de fechas, muestra los últimos 12 meses.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="mt-6 flex flex-wrap items-end gap-3">
        <div className="w-44">
          <Input
            id="from"
            type="date"
            label="Desde"
            value={range.from}
            onChange={(event) => setRange((prev) => ({ ...prev, from: event.target.value }))}
          />
        </div>
        <div className="w-44">
          <Input
            id="to"
            type="date"
            label="Hasta"
            value={range.to}
            onChange={(event) => setRange((prev) => ({ ...prev, to: event.target.value }))}
          />
        </div>
        <Button type="submit" className="px-3 py-2.5 text-sm">
          Filtrar
        </Button>
        <Button
          type="button"
          onClick={handleClear}
          className="bg-slate-600 px-3 py-2.5 text-sm hover:bg-slate-700"
        >
          Limpiar
        </Button>
      </form>

      {rangeError && (
        <div className="mt-4">
          <Alert>{rangeError}</Alert>
        </div>
      )}

      <div className="mt-6">
        {indicatorsQuery.isPending && <p className="text-slate-500">Cargando indicadores…</p>}
        {indicatorsQuery.isError && <Alert>{indicatorsQuery.error.message}</Alert>}
        {data && (
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
              <IndicatorCard label="Marcos recibidos" value={data.framesReceived} />
              <IndicatorCard label="Anteojos entregados" value={data.assignmentsDelivered} />
              <IndicatorCard label="Beneficiarios atendidos" value={data.applicantsServed} />
              <IndicatorCard label="Turnos asistidos" value={data.appointmentsAttended} />
              <IndicatorCard label="Turnos no asistidos" value={data.appointmentsMissed} />
              <IndicatorCard
                label="Asistencia a turnos"
                value={formatPercentage(data.appointmentsAttendanceRate)}
              />
              <IndicatorCard label="Envíos entregados" value={data.shipmentsDelivered} />
            </div>

            <div className="rounded-lg border border-slate-200 bg-white p-4">
              <h3 className="text-sm font-semibold text-slate-900">Entregas por mes</h3>
              <div className="mt-3">
                <MonthlyDeliveriesChart byMonth={data.byMonth} />
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}
