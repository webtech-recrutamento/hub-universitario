import { api } from './api'
import type { Activity, Registration, RegistrationInput } from '../types/activity'

export async function getActivities(search = ''): Promise<Activity[]> {
  const response = await api.get<Activity[]>('/activities', {
    params: search ? { search }: undefined,
  })
  return response.data
}

export async function getActivity(id: number): Promise<Activity> {
  const response = await api.get<Activity>(`/activities/${id}`)
  return response.data
}

export async function getRegistrations(activityId: number): Promise<Registration[]> {
  const response = await api.get<Registration[]>(`/activities/${activityId}/registrations`)
  return response.data
}

export async function createRegistration(
  activityId: number,
  input: RegistrationInput,
): Promise<Registration> {
  const response = await api.post<Registration>(`/activities/${activityId}/registrations`, input)
  return response.data
}
