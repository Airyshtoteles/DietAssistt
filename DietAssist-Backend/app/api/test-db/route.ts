import { NextResponse } from 'next/server';
import { supabase } from '@/lib/supabase';

export async function GET() {
  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'NOT_SET';
  const anonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || '';
  const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY || '';
  const geminiKey = process.env.GEMINI_API_KEY || '';

  // 1. Uji kueri langsung ke users_profile
  const { data: usersProfileData, error: usersProfileError } = await supabase
    .from('users_profile')
    .select('*')
    .limit(1);

  // 2. Uji kueri langsung ke profiles
  const { data: profilesData, error: profilesError } = await supabase
    .from('profiles')
    .select('*')
    .limit(1);

  // 3. Uji kueri langsung ke food_logs
  const { data: foodLogsData, error: foodLogsError } = await supabase
    .from('food_logs')
    .select('*')
    .limit(1);

  return NextResponse.json({
    success: true,
    supabaseUrl: supabaseUrl,
    anonKeyLength: anonKey.length,
    serviceKeyLength: serviceKey.length,
    geminiKeyLength: geminiKey.length,
    
    // Hasil Pengujian Tabel
    testUsersProfile: {
      exists: !usersProfileError || usersProfileError.code !== 'PGRST116' && usersProfileError.message.indexOf('does not exist') === -1,
      error: usersProfileError ? { code: usersProfileError.code, message: usersProfileError.message } : null,
    },
    testProfiles: {
      exists: !profilesError || profilesError.code !== 'PGRST116' && profilesError.message.indexOf('does not exist') === -1,
      error: profilesError ? { code: profilesError.code, message: profilesError.message } : null,
    },
    testFoodLogs: {
      exists: !foodLogsError || foodLogsError.code !== 'PGRST116' && foodLogsError.message.indexOf('does not exist') === -1,
      error: foodLogsError ? { code: foodLogsError.code, message: foodLogsError.message } : null,
    }
  });
}
