import { FRAME_STATUS_LABELS, FRAME_STATUS_STYLES } from '../labels'

export function FrameStatusBadge({ status }) {
  const style = FRAME_STATUS_STYLES[status] ?? 'bg-slate-100 text-slate-600'
  return (
    <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${style}`}>
      {FRAME_STATUS_LABELS[status] ?? status}
    </span>
  )
}
