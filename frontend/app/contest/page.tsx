'use client';

import { useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { apiService, Contest, Problem, SubmissionResponse, LeaderboardEntry, SubmissionStatus } from '@/lib/api';

export default function ContestPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  
  const contestId = searchParams.get('contestId') || '';
  const username = searchParams.get('username') || '';

  const [contest, setContest] = useState<Contest | null>(null);
  const [problems, setProblems] = useState<Problem[]>([]);
  const [selectedProblem, setSelectedProblem] = useState<Problem | null>(null);
  const [code, setCode] = useState('');
  const [language, setLanguage] = useState('java');
  const [submission, setSubmission] = useState<SubmissionResponse | null>(null);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [timeRemaining, setTimeRemaining] = useState<{
    days: number;
    hours: number;
    minutes: number;
    seconds: number;
    status: 'upcoming' | 'active' | 'ended';
  } | null>(null);

  useEffect(() => {
    if (!contestId || !username) {
      router.push('/');
      return;
    }

    loadContest();
    loadLeaderboard();
    
    // Poll leaderboard every 20 seconds
    const leaderboardInterval = setInterval(loadLeaderboard, 20000);
    
    return () => clearInterval(leaderboardInterval);
  }, [contestId, username, router]);

  useEffect(() => {
    if (submission && (submission.status === SubmissionStatus.PENDING || submission.status === SubmissionStatus.RUNNING)) {
      // Poll submission status every 2 seconds
      const submissionInterval = setInterval(async () => {
        try {
          const updatedSubmission = await apiService.getSubmission(submission.submissionId);
          setSubmission(updatedSubmission);
          
          if (updatedSubmission.status !== SubmissionStatus.PENDING && 
              updatedSubmission.status !== SubmissionStatus.RUNNING) {
            clearInterval(submissionInterval);
            loadLeaderboard(); // Refresh leaderboard after submission completes
          }
        } catch (err) {
          console.error('Error polling submission:', err);
        }
      }, 2000);

      return () => clearInterval(submissionInterval);
    }
  }, [submission]);

  // Timer effect
  useEffect(() => {
    if (!contest) return;

    const updateTimer = () => {
      const now = new Date().getTime();
      const startTime = new Date(contest.startTime).getTime();
      const endTime = new Date(contest.endTime).getTime();

      let targetTime: number;
      let status: 'upcoming' | 'active' | 'ended';

      if (now < startTime) {
        // Contest hasn't started yet
        targetTime = startTime;
        status = 'upcoming';
      } else if (now >= endTime) {
        // Contest has ended
        status = 'ended';
        setTimeRemaining({
          days: 0,
          hours: 0,
          minutes: 0,
          seconds: 0,
          status: 'ended',
        });
        return;
      } else {
        // Contest is active
        targetTime = endTime;
        status = 'active';
      }

      const difference = targetTime - now;
      const days = Math.floor(difference / (1000 * 60 * 60 * 24));
      const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((difference % (1000 * 60)) / 1000);

      setTimeRemaining({ days, hours, minutes, seconds, status });
    };

    // Update immediately
    updateTimer();

    // Update every second
    const timerInterval = setInterval(updateTimer, 1000);

    return () => clearInterval(timerInterval);
  }, [contest]);

  const loadContest = async () => {
    try {
      setLoading(true);
      setError('');
      const contestData = await apiService.getContest(contestId);
      setContest(contestData);
      setProblems(contestData.problems || []);
      if (contestData.problems && contestData.problems.length > 0) {
        setSelectedProblem(contestData.problems[0]);
        setCode(getStarterCode(contestData.problems[0], language));
      }
    } catch (err: any) {
      console.error('Error loading contest:', err);
      // Handle string response from backend (404 case)
      const errorMsg = typeof err.response?.data === 'string' 
        ? err.response.data 
        : err.response?.data?.message || err.message || 'Failed to load contest';
      setError(errorMsg);
      setContest(null);
    } finally {
      setLoading(false);
    }
  };

  const loadLeaderboard = async () => {
    try {
      const leaderboardData = await apiService.getLeaderboard(contestId);
      setLeaderboard(leaderboardData);
    } catch (err: any) {
      // If contest not found, don't show error in leaderboard
      if (err.response?.status === 404) {
        setLeaderboard([]);
      } else {
        console.error('Error loading leaderboard:', err);
      }
    }
  };

  const handleProblemSelect = (problem: Problem) => {
    setSelectedProblem(problem);
    setCode(getStarterCode(problem, language));
    setSubmission(null);
  };

  const handleSubmit = async () => {
    if (!selectedProblem || !code.trim()) {
      setError('Please select a problem and write some code');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      const response = await apiService.submitCode({
        contestId,
        problemId: selectedProblem.problemId,
        username,
        code,
        language,
      });
      setSubmission(response);
    } catch (err: any) {
      setError(err.response?.data || 'Failed to submit code');
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusColor = (status: SubmissionStatus) => {
    switch (status) {
      case SubmissionStatus.ACCEPTED:
        return 'text-green-600 bg-green-50';
      case SubmissionStatus.WRONG_ANSWER:
      case SubmissionStatus.RUNTIME_ERROR:
      case SubmissionStatus.COMPILATION_ERROR:
      case SubmissionStatus.TIME_LIMIT_EXCEEDED:
        return 'text-red-600 bg-red-50';
      case SubmissionStatus.RUNNING:
        return 'text-blue-600 bg-blue-50';
      default:
        return 'text-yellow-600 bg-yellow-50';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-xl text-gray-600">Loading contest...</div>
      </div>
    );
  }

  if (!contest && !loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full text-center">
          <div className="text-red-600 text-xl font-semibold mb-4">Contest not found</div>
          <div className="text-gray-600 mb-6">
            {error || `The contest "${contestId}" could not be found.`}
          </div>
          <div className="text-sm text-gray-500 mb-4">
            Make sure you entered the correct contest ID (case-sensitive).
          </div>
          <button
            onClick={() => router.push('/')}
            className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 transition"
          >
            Go Back to Join Page
          </button>
          <div className="mt-4 text-sm text-gray-500">
            Try: <span className="font-mono font-semibold text-indigo-600">CONTEST-001</span>
          </div>
        </div>
      </div>
    );
  }

  const formatTime = (time: number) => String(time).padStart(2, '0');

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex justify-between items-center">
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-gray-900">{contest.name}</h1>
            <p className="text-sm text-gray-600">Welcome, {username}</p>
          </div>
          
          {/* Timer */}
          {timeRemaining && (
            <div className="flex items-center gap-4 mr-4">
              {timeRemaining.status === 'upcoming' && (
                <div className="text-center">
                  <div className="text-xs text-gray-500 mb-1">Contest Starts In</div>
                  <div className="flex gap-2 items-center">
                    {timeRemaining.days > 0 && (
                      <div className="bg-blue-100 text-blue-800 px-3 py-2 rounded-lg">
                        <div className="text-2xl font-bold">{formatTime(timeRemaining.days)}</div>
                        <div className="text-xs">Days</div>
                      </div>
                    )}
                    <div className="bg-blue-100 text-blue-800 px-3 py-2 rounded-lg">
                      <div className="text-2xl font-bold">{formatTime(timeRemaining.hours)}</div>
                      <div className="text-xs">Hours</div>
                    </div>
                    <div className="bg-blue-100 text-blue-800 px-3 py-2 rounded-lg">
                      <div className="text-2xl font-bold">{formatTime(timeRemaining.minutes)}</div>
                      <div className="text-xs">Minutes</div>
                    </div>
                    <div className="bg-blue-100 text-blue-800 px-3 py-2 rounded-lg">
                      <div className="text-2xl font-bold">{formatTime(timeRemaining.seconds)}</div>
                      <div className="text-xs">Seconds</div>
                    </div>
                  </div>
                </div>
              )}
              {timeRemaining.status === 'active' && (
                <div className="text-center">
                  <div className="text-xs text-green-600 mb-1 font-semibold">Time Remaining</div>
                  <div className="flex gap-2 items-center">
                    {timeRemaining.days > 0 && (
                      <div className="bg-green-100 text-green-800 px-3 py-2 rounded-lg">
                        <div className="text-2xl font-bold">{formatTime(timeRemaining.days)}</div>
                        <div className="text-xs">Days</div>
                      </div>
                    )}
                    <div className="bg-green-100 text-green-800 px-3 py-2 rounded-lg">
                      <div className="text-2xl font-bold">{formatTime(timeRemaining.hours)}</div>
                      <div className="text-xs">Hours</div>
                    </div>
                    <div className="bg-green-100 text-green-800 px-3 py-2 rounded-lg">
                      <div className="text-2xl font-bold">{formatTime(timeRemaining.minutes)}</div>
                      <div className="text-xs">Minutes</div>
                    </div>
                    <div className="bg-green-100 text-green-800 px-3 py-2 rounded-lg">
                      <div className="text-2xl font-bold">{formatTime(timeRemaining.seconds)}</div>
                      <div className="text-xs">Seconds</div>
                    </div>
                  </div>
                </div>
              )}
              {timeRemaining.status === 'ended' && (
                <div className="text-center">
                  <div className="bg-red-100 text-red-800 px-4 py-2 rounded-lg">
                    <div className="text-lg font-bold">Contest Ended</div>
                  </div>
                </div>
              )}
            </div>
          )}
          
          <button
            onClick={() => router.push('/')}
            className="px-4 py-2 text-sm text-gray-600 hover:text-gray-900"
          >
            Leave Contest
          </button>
        </div>
      </div>

      <div className="flex h-[calc(100vh-80px)]">
        {/* Left Panel - Problems & Code Editor */}
        <div className="w-1/2 flex flex-col border-r border-gray-200">
          {/* Problem Selector */}
          <div className="bg-white border-b border-gray-200 p-4">
            <h2 className="text-lg font-semibold mb-3">Problems</h2>
            <div className="flex gap-2">
              {problems.map((problem) => (
                <button
                  key={problem.id}
                  onClick={() => handleProblemSelect(problem)}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                    selectedProblem?.id === problem.id
                      ? 'bg-indigo-600 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  {problem.title}
                </button>
              ))}
            </div>
          </div>

          {/* Problem Description */}
          {selectedProblem && (
            <div className="bg-white flex-1 overflow-y-auto p-6">
              <h3 className="text-xl font-bold mb-4">{selectedProblem.title}</h3>
              <div className="prose max-w-none">
                <pre className="whitespace-pre-wrap text-sm text-gray-700 bg-gray-50 p-4 rounded-lg mb-4">
                  {selectedProblem.description}
                </pre>
              </div>
              <div className="mt-4 pt-4 border-t border-gray-200">
                <p className="text-sm text-gray-600">
                  Time Limit: {selectedProblem.timeLimitSeconds}s | Memory Limit: {selectedProblem.memoryLimitMB}MB
                </p>
              </div>
            </div>
          )}

          {/* Code Editor */}
          <div className="bg-gray-900 flex-1 flex flex-col">
            <div className="px-4 py-2 bg-gray-800 border-b border-gray-700 flex justify-between items-center">
              <h3 className="text-sm font-medium text-gray-300">Code Editor</h3>
              <select 
                value={language}
                onChange={(e) => {
                  const newLanguage = e.target.value;
                  setLanguage(newLanguage);
                  // Update starter code when language changes
                  if (selectedProblem) {
                    setCode(getStarterCode(selectedProblem, newLanguage));
                  }
                }}
                className="bg-gray-700 text-gray-200 text-sm rounded px-2 py-1 border border-gray-600 cursor-pointer hover:bg-gray-600 transition-colors"
              >
                <option value="java">Java</option>
                <option value="python">Python</option>
                <option value="javascript">JavaScript</option>
                <option value="c">C</option>
                <option value="cpp">C++</option>
              </select>
            </div>
            <textarea
              value={code}
              onChange={(e) => setCode(e.target.value)}
              className="flex-1 w-full bg-gray-900 text-green-400 font-mono text-sm p-4 outline-none resize-none"
              placeholder={`Write your ${language} code here...`}
            />
          </div>

          {/* Submit Button & Status */}
          <div className="bg-white border-t border-gray-200 p-4">
            <button
              onClick={handleSubmit}
              disabled={submitting || !code.trim()}
              className="w-full bg-indigo-600 text-white py-3 rounded-lg font-semibold hover:bg-indigo-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
            >
              {submitting ? 'Submitting...' : 'Submit Code'}
            </button>
            
            {error && (
              <div className="mt-3 p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">
                {error}
              </div>
            )}

            {submission && (
              <div className={`mt-3 p-3 rounded-lg ${getStatusColor(submission.status)}`}>
                <div className="font-semibold">Status: {submission.status}</div>
                <div className="text-sm mt-1">Language: {submission.language}</div>
                {submission.testCasesPassed !== undefined && submission.totalTestCases !== undefined && (
                  <div className="text-sm mt-1">
                    Test Cases: {submission.testCasesPassed}/{submission.totalTestCases}
                  </div>
                )}
                {submission.errorMessage && (
                  <div className="text-sm mt-2 whitespace-pre-wrap">{submission.errorMessage}</div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Right Panel - Leaderboard */}
        <div className="w-1/2 bg-white overflow-y-auto">
          <div className="p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold">Leaderboard</h2>
              <div className="text-sm text-gray-500">
                Auto-refreshes every 20s
              </div>
            </div>
            
            {leaderboard.length === 0 ? (
              <div className="text-center text-gray-500 py-12">
                <div className="text-4xl mb-4">🏆</div>
                <div className="text-lg font-semibold mb-2">No submissions yet</div>
                <div className="text-sm">Be the first to submit and claim the top spot!</div>
              </div>
            ) : (
              <div className="space-y-2">
                {/* Header */}
                <div className="grid grid-cols-12 gap-2 px-4 py-2 bg-gray-100 rounded-lg text-sm font-semibold text-gray-700 mb-2">
                  <div className="col-span-1 text-center">Rank</div>
                  <div className="col-span-5">Username</div>
                  <div className="col-span-2 text-center">Solved</div>
                  <div className="col-span-2 text-center">Accepted</div>
                  <div className="col-span-2 text-center">Total</div>
                </div>
                
                {/* Leaderboard Entries */}
                {leaderboard.map((entry, index) => {
                  const isCurrentUser = entry.username === username;
                  const rank = index + 1;
                  const getRankBadge = () => {
                    if (rank === 1) return '🥇';
                    if (rank === 2) return '🥈';
                    if (rank === 3) return '🥉';
                    return null;
                  };

                  return (
                    <div
                      key={entry.username}
                      className={`grid grid-cols-12 gap-2 items-center p-3 rounded-lg transition-all ${
                        isCurrentUser
                          ? 'bg-indigo-50 border-2 border-indigo-400 shadow-md'
                          : rank <= 3
                          ? 'bg-gradient-to-r from-yellow-50 to-transparent border border-yellow-200'
                          : 'bg-gray-50 border border-gray-200 hover:bg-gray-100'
                      }`}
                    >
                      {/* Rank */}
                      <div className="col-span-1 flex items-center justify-center">
                        {getRankBadge() ? (
                          <span className="text-2xl">{getRankBadge()}</span>
                        ) : (
                          <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm ${
                            rank <= 3
                              ? 'bg-yellow-400 text-yellow-900'
                              : 'bg-gray-300 text-gray-700'
                          }`}>
                            {rank}
                          </div>
                        )}
                      </div>
                      
                      {/* Username */}
                      <div className="col-span-5">
                        <div className={`font-semibold ${
                          isCurrentUser ? 'text-indigo-900' : 'text-gray-900'
                        }`}>
                          {entry.username}
                          {isCurrentUser && (
                            <span className="ml-2 text-xs bg-indigo-600 text-white px-2 py-0.5 rounded">You</span>
                          )}
                        </div>
                      </div>
                      
                      {/* Problems Solved */}
                      <div className="col-span-2 text-center">
                        <div className={`text-xl font-bold ${
                          rank === 1 ? 'text-yellow-600' :
                          rank === 2 ? 'text-gray-600' :
                          rank === 3 ? 'text-orange-600' :
                          'text-indigo-600'
                        }`}>
                          {entry.totalProblemsSolved}
                        </div>
                      </div>
                      
                      {/* Accepted Submissions */}
                      <div className="col-span-2 text-center">
                        <div className="text-sm font-semibold text-green-600">
                          {entry.acceptedSubmissions}
                        </div>
                      </div>
                      
                      {/* Total Submissions */}
                      <div className="col-span-2 text-center">
                        <div className="text-sm text-gray-600">
                          {entry.totalSubmissions}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function getStarterCode(problem: Problem | null, language: string = 'java'): string {
  if (!problem) return '';
  
  switch (language) {
    case 'java':
      return `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Write your solution here
        
    }
}`;
    
    case 'python':
      return `# Write your solution here
import sys

if __name__ == "__main__":
    # Read input from stdin
    data = sys.stdin.read().strip()
    
    # Write your solution here
    
`;
    
    case 'javascript':
      return `// Write your solution here
const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

rl.on('line', (line) => {
    // Write your solution here
    
    rl.close();
});
`;
    
    case 'c':
      return `#include <stdio.h>

int main() {
    // Write your solution here
    
    return 0;
}
`;
    
    case 'cpp':
      return `#include <iostream>
using namespace std;

int main() {
    // Write your solution here
    
    return 0;
}
`;
    
    default:
      return `// Write your solution here`;
  }
}
