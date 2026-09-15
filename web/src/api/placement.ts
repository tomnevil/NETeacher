import request from './request'
import type { Result, PlacementQuestion, PlacementResult, PlacementAnswer } from './types'

export function getPlacementQuestions() {
  return request.get<Result<PlacementQuestion[]>>('/placement')
}

export function submitPlacement(answers: PlacementAnswer[]) {
  return request.post<Result<PlacementResult>>('/placement', { answers })
}
