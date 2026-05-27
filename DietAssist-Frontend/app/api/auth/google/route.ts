import { NextResponse } from 'next/server';
import { supabase } from '@/lib/supabase';
import { createHash } from 'crypto';

function toUUID(str: string): string {
  if (!str) return '00000000-0000-0000-0000-000000000000';
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  if (uuidRegex.test(str)) {
    return str;
  }
  const hash = createHash('md5').update(str).digest('hex');
  return `${hash.slice(0, 8)}-${hash.slice(8, 12)}-${hash.slice(12, 16)}-${hash.slice(16, 20)}-${hash.slice(20, 32)}`;
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { idToken, name, email, userId, daily_calorie_target, weight, height } = body;

    if (!idToken && !userId) {
      return NextResponse.json(
        { error: 'idToken atau userId wajib disediakan' },
        { status: 400 }
      );
    }

    let finalUserId = userId;
    let finalEmail = email || 'user@dietassist.com';
    let finalName = name || 'Pengguna DietAssist';

    // Opsional: Lakukan verifikasi idToken langsung ke Google API jika idToken disertakan
    if (idToken) {
      try {
        const googleVerifyRes = await fetch(
          `https://oauth2.googleapis.com/tokeninfo?id_token=${idToken}`
        );
        if (googleVerifyRes.ok) {
          const payload = await googleVerifyRes.json();
          finalEmail = payload.email || finalEmail;
          finalName = payload.name || finalName;
          // Sub dari payload Google bisa digunakan sebagai userId unik jika tidak disediakan
          if (!finalUserId) {
            finalUserId = payload.sub;
          }
        }
      } catch (err) {
        console.warn("Gagal verifikasi idToken Google secara online, menggunakan fallback data klien:", err);
      }
    }

    if (!finalUserId) {
      return NextResponse.json(
        { error: 'Gagal mendeteksi User ID yang unik' },
        { status: 400 }
      );
    }

    // Ubah finalUserId menjadi UUID yang valid dan konsisten
    finalUserId = toUUID(finalUserId);

    // Cek apakah user_profile sudah ada
    const { data: profile, error: selectError } = await supabase
      .from('users_profile')
      .select('*')
      .eq('id', finalUserId)
      .single();

    let userProfile = profile;

    if (selectError || !profile) {
      // Pastikan user terdaftar di auth.users dengan ID finalUserId agar tidak melanggar foreign key constraint
      try {
        await supabase.auth.admin.createUser({
          id: finalUserId,
          email: finalEmail,
          email_confirm: true,
          user_metadata: { name: finalName }
        });
      } catch (err) {
        console.warn("User mungkin sudah terdaftar di auth.users:", err);
      }

      // Jika profile belum ada, buat baru (Upsert / Insert)
      const { data: newProfile, error: insertError } = await supabase
        .from('users_profile')
        .insert({
          id: finalUserId,
          name: finalName,
          email: finalEmail,
          daily_calorie_target: daily_calorie_target || 2000, // Gunakan nilai masukan jika ada
          weight: weight || 0.0, // 0.0 menandakan profil belum di-setup
          height: height || 0.0, // 0.0 menandakan profil belum di-setup
        })
        .select()
        .single();

      if (insertError) {
        console.error("Gagal membuat users_profile baru:", insertError);
        // Fallback jika database Supabase belum terhubung sempurna
        userProfile = {
          id: finalUserId,
          name: finalName,
          email: finalEmail,
          daily_calorie_target: daily_calorie_target || 2000,
          weight: weight || 0.0,
          height: height || 0.0,
        };
      } else {
        userProfile = newProfile;
      }
    } else {
      // Jika profil sudah ada, cek apakah ada perubahan/update profil target dari aplikasi (onboarding/settings)
      if (
        (name && name !== profile.name) ||
        (daily_calorie_target !== undefined && daily_calorie_target !== profile.daily_calorie_target) ||
        (weight !== undefined && weight !== profile.weight) ||
        (height !== undefined && height !== profile.height)
      ) {
        const { data: updatedProfile, error: updateError } = await supabase
          .from('users_profile')
          .update({
            name: name || profile.name,
            daily_calorie_target: daily_calorie_target !== undefined ? daily_calorie_target : profile.daily_calorie_target,
            weight: weight !== undefined ? weight : profile.weight,
            height: height !== undefined ? height : profile.height,
            updated_at: new Date().toISOString()
          })
          .eq('id', finalUserId)
          .select()
          .single();

        if (updateError) {
          console.error("Gagal memperbarui users_profile:", updateError);
        } else {
          userProfile = updatedProfile;
        }
      }
    }

    return NextResponse.json({
      success: true,
      message: 'Autentikasi berhasil',
      user: userProfile,
    });
  } catch (error: any) {
    console.error('Error di API Auth Google:', error);
    return NextResponse.json(
      { error: 'Internal Server Error', details: error.message },
      { status: 500 }
    );
  }
}
