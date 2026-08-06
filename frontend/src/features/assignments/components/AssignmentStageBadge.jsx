import { ASSIGNMENT_STAGE_LABELS, ASSIGNMENT_STAGE_STYLES, assignmentStage } from '../labels'

export function AssignmentStageBadge({ assignment }) {
  const stage = assignmentStage(assignment)
  return (
    <span
      className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${ASSIGNMENT_STAGE_STYLES[stage]}`}
    >
      {ASSIGNMENT_STAGE_LABELS[stage]}
    </span>
  )
}
