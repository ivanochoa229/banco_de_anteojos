import { Link } from 'react-router-dom'
import { formatDateTime } from '../../../lib/dates'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../labels'
import { FrameStatusBadge } from './FrameStatusBadge'

// Calibre · puente · patilla, la forma en que vienen grabadas en el marco.
function formatMeasurements({ lensWidthMm, bridgeWidthMm, templeLengthMm }) {
  const measurements = [lensWidthMm, bridgeWidthMm, templeLengthMm]
  if (measurements.every((value) => value === null || value === undefined)) return '—'
  return measurements.map((value) => value ?? '?').join(' · ')
}

// donorNameById: solo lo pasa el inventario general, donde importa de quién vino cada marco.
// En la ficha del donante la columna sobra.
export function FramesTable({ frames, donorNameById }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
      <table className="w-full text-left text-sm">
        <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
          <tr>
            <th className="px-4 py-3">Precinto</th>
            <th className="px-4 py-3">Tipo</th>
            <th className="px-4 py-3">Material</th>
            <th className="px-4 py-3">Medidas (mm)</th>
            {donorNameById && <th className="px-4 py-3">Donante</th>}
            <th className="px-4 py-3">Estado</th>
            <th className="px-4 py-3">Ingreso</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {frames.map((frame) => (
            <tr key={frame.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-medium text-slate-900">{frame.sealCode}</td>
              <td className="px-4 py-3 text-slate-600">
                {FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType}
              </td>
              <td className="px-4 py-3 text-slate-600">
                {FRAME_MATERIAL_LABELS[frame.material] ?? frame.material}
              </td>
              <td className="px-4 py-3 text-slate-600">{formatMeasurements(frame)}</td>
              {donorNameById && (
                <td className="px-4 py-3">
                  <Link
                    to={`/donors/${frame.donorId}/frames`}
                    className="font-medium text-sky-700 hover:underline"
                  >
                    {donorNameById[frame.donorId] ?? `Donante #${frame.donorId}`}
                  </Link>
                </td>
              )}
              <td className="px-4 py-3">
                <FrameStatusBadge status={frame.status} />
              </td>
              <td className="px-4 py-3 text-slate-600">{formatDateTime(frame.receivedAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
