import { NextResponse } from 'next/server';

export async function GET() {
  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'NOT_SET';
  const anonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || '';
  const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY || '';
  const geminiKey = process.env.GEMINI_API_KEY || '';

  return NextResponse.json({
    success: true,
    supabaseUrl: supabaseUrl,
    anonKeyLength: anonKey.length,
    anonKeyPrefix: anonKey ? anonKey.substring(0, 10) + '...' : 'empty',
    serviceKeyLength: serviceKey.length,
    serviceKeyPrefix: serviceKey ? serviceKey.substring(0, 10) + '...' : 'empty',
    geminiKeyLength: geminiKey.length,
    geminiKeyPrefix: geminiKey ? geminiKey.substring(0, 8) + '...' : 'empty',
  });
}
