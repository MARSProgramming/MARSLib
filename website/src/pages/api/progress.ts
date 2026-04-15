import type { APIRoute } from 'astro';

/**
 * Handles fetching progress from Cloudflare KV
 */
export const GET: APIRoute = async ({ request, locals }) => {
  const url = new URL(request.url);
  const syncId = url.searchParams.get('syncId');

  if (!syncId) {
    return new Response(JSON.stringify({ error: 'Missing Sync ID' }), { status: 400 });
  }

  try {
    // Access the KV Namespace injected by the @astrojs/cloudflare adapter
    const kv = (locals as any)?.runtime?.env?.MARSLIB_KV;
    
    if (!kv) {
        return new Response(JSON.stringify({ error: 'KV Database not bound. Check Cloudflare Dashboard.' }), { status: 500 });
    }

    const data = await kv.get(`progress:${syncId}`);
    
    if (data) {
        return new Response(data, {
            status: 200,
            headers: { 'Content-Type': 'application/json' }
        });
    } else {
        return new Response(JSON.stringify({ data: {} }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' }
        });
    }

  } catch (error) {
    console.error("KV GET Error:", error);
    return new Response(JSON.stringify({ error: 'Failed to access database' }), { status: 500 });
  }
};

/**
 * Handles saving progress to Cloudflare KV
 */
export const POST: APIRoute = async ({ request, locals }) => {
  try {
    const body = await request.json();
    const syncId = body.syncId;
    const progressData = body.progressData;

    if (!syncId || !progressData) {
      return new Response(JSON.stringify({ error: 'Payload must contain syncId and progressData' }), { status: 400 });
    }

    const kv = (locals as any)?.runtime?.env?.MARSLIB_KV;
    
    if (!kv) {
        return new Response(JSON.stringify({ error: 'KV Database not bound. Check Cloudflare Dashboard.' }), { status: 500 });
    }

    // Save with no expiration so the progress lives forever
    await kv.put(`progress:${syncId}`, JSON.stringify(progressData));

    return new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  } catch (error) {
    console.error("KV POST Error:", error);
    return new Response(JSON.stringify({ error: 'Failed to save progress to database' }), { status: 500 });
  }
};
