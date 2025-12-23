// Resolves image URLs returned by the backend.
// - If URL is absolute (http/https), keep as-is.
// - If URL is relative (e.g. /uploads/..., uploads/...), prefix backend origin.

const BACKEND_ORIGIN = 'http://localhost:8080';

export default function resolveImageUrl(url) {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;

  // Ensure there is exactly one slash between origin and path
  const path = url.startsWith('/') ? url : `/${url}`;
  return `${BACKEND_ORIGIN}${path}`;
}
