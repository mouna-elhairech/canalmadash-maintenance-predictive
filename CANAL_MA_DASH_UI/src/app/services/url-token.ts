const STORAGE_KEY = 'canalmadash_auth';

export function avecToken(url: string): string {
  let token: string | null = null;
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    token = raw ? JSON.parse(raw).token : null;
  } catch {
    token = null;
  }
  if (!token) {
    return url;
  }
  return url + (url.includes('?') ? '&' : '?') + 'token=' + encodeURIComponent(token);
}