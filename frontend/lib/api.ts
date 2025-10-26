import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface Contest {
  id: number;
  contestId: string;
  name: string;
  description: string;
  problems: Problem[];
  startTime: string;
  endTime: string;
}

export interface Problem {
  id: number;
  problemId: string;
  title: string;
  description: string;
  inputTestCases: string[];
  expectedOutputs: string[];
  timeLimitSeconds: number;
  memoryLimitMB: number;
}

export interface SubmissionRequest {
  contestId: string;
  problemId: string;
  username: string;
  code: string;
  language: string;
}

export interface SubmissionResponse {
  submissionId: string;
  username: string;
  problemId: string;
  status: SubmissionStatus;
  language: string;
  errorMessage?: string;
  testCasesPassed?: number;
  totalTestCases?: number;
  submittedAt: string;
  processedAt?: string;
}

export enum SubmissionStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  ACCEPTED = 'ACCEPTED',
  WRONG_ANSWER = 'WRONG_ANSWER',
  TIME_LIMIT_EXCEEDED = 'TIME_LIMIT_EXCEEDED',
  MEMORY_LIMIT_EXCEEDED = 'MEMORY_LIMIT_EXCEEDED',
  RUNTIME_ERROR = 'RUNTIME_ERROR',
  COMPILATION_ERROR = 'COMPILATION_ERROR',
}

export interface LeaderboardEntry {
  username: string;
  totalSubmissions: number;
  acceptedSubmissions: number;
  totalProblemsSolved: number;
}

export const apiService = {
  getContest: async (contestId: string): Promise<Contest> => {
    try {
      const response = await api.get(`/contests/${contestId}`);
      console.log('API Response:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('API Error:', error);
      console.error('Response:', error.response?.data);
      throw error;
    }
  },

  getProblems: async (contestId: string): Promise<Problem[]> => {
    const response = await api.get(`/contests/${contestId}/problems`);
    return response.data;
  },

  submitCode: async (request: SubmissionRequest): Promise<SubmissionResponse> => {
    const response = await api.post('/submissions', request);
    return response.data;
  },

  getSubmission: async (submissionId: string): Promise<SubmissionResponse> => {
    const response = await api.get(`/submissions/${submissionId}`);
    return response.data;
  },

  getLeaderboard: async (contestId: string): Promise<LeaderboardEntry[]> => {
    const response = await api.get(`/contests/${contestId}/leaderboard`);
    return response.data;
  },
};
