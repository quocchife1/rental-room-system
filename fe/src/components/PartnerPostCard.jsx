import React from 'react';
import { Link } from 'react-router-dom';
import resolveImageUrl from '../utils/resolveImageUrl';

export default function PartnerPostCard({ post }) {
  const getThumbnail = (post) => {
    if (post.imageUrls && post.imageUrls.length > 0) {
      return resolveImageUrl(post.imageUrls[0]);
    }
    return 'https://placehold.co/600x400?text=No+Image';
  };

  const tier = post.postType;
  const tierConfig = {
    NORMAL: {
      card: 'border-gray-100',
      badge: null
    },
    VIP1: {
      card: 'border-blue-200 ring-1 ring-blue-100',
      badge: { text: 'VIP 1 • Silver', bg: 'from-blue-500 to-indigo-500' }
    },
    VIP2: {
      card: 'border-amber-200 ring-1 ring-amber-100',
      badge: { text: 'VIP 2 • Gold ⭐', bg: 'from-amber-400 to-orange-500' }
    },
    VIP3: {
      card: 'border-purple-200 ring-1 ring-purple-100',
      badge: { text: 'VIP 3 • Diamond', bg: 'from-fuchsia-500 via-purple-500 to-pink-500' }
    }
  };

  const activeTier = tierConfig[tier] || tierConfig.NORMAL;

  return (
    <Link
      to={`/partner-posts/${post.id}`}
      className={`group block bg-[color:var(--app-surface-solid)] rounded-2xl overflow-hidden shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border border-[color:var(--app-border)] h-full flex flex-col ${activeTier.card}`}
    >
      {/* Image */}
      <div className="relative h-56 overflow-hidden">
        <img
          src={getThumbnail(post)}
          alt={post.title}
          className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
          onError={(e) => { e.target.onerror = null; e.target.src = 'https://placehold.co/600x400?text=Error'; }}
        />

        {activeTier.badge && (
          <div className="absolute top-3 left-3 z-10">
            <span className={`px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide shadow-md bg-gradient-to-r ${activeTier.badge.bg} text-white flex items-center gap-1`}>
              <span>👑</span> {activeTier.badge.text}
            </span>
          </div>
        )}

        <div className="absolute bottom-0 left-0 w-full h-2/3 bg-gradient-to-t from-black/80 via-black/40 to-transparent"></div>

        <div className="absolute bottom-4 left-4 right-4 text-white z-10">
          <p className="text-xl font-bold truncate tracking-tight">
            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(post.price)}
            <span className="text-xs font-normal opacity-80 ml-1">/ tháng</span>
          </p>
        </div>
      </div>

      {/* Content */}
      <div className="p-5 flex flex-col flex-1">
        <h3 className="text-base font-bold text-[color:var(--app-text)] group-hover:text-[color:var(--app-primary)] transition-colors line-clamp-2 leading-snug mb-3">
          {post.title}
        </h3>

        <div className="space-y-2 mb-4 flex-1">
          <div className="flex items-start gap-2 text-xs text-[color:var(--app-muted)]">
            <span className="mt-0.5 text-[color:var(--app-primary)] flex-shrink-0">📍</span>
            <span className="line-clamp-2">{post.address}</span>
          </div>
          <div className="flex items-center gap-3 text-xs text-[color:var(--app-muted)] font-medium mt-2">
            <div className="flex items-center gap-1 bg-[color:var(--app-bg)] px-2 py-1 rounded-md border border-[color:var(--app-border)]">
              <span>📐</span> {post.area} m²
            </div>
            <div className="flex items-center gap-1 bg-[color:var(--app-bg)] px-2 py-1 rounded-md border border-[color:var(--app-border)]">
              <span>🕒</span> {new Date(post.createdAt || Date.now()).toLocaleDateString('vi-VN')}
            </div>
          </div>
        </div>

        <div className="pt-4 border-t border-[color:var(--app-border)] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-[color:var(--app-primary-soft)] flex items-center justify-center text-[color:var(--app-primary)] font-bold text-xs border border-[color:var(--app-border)] shadow-sm">
              {post.partnerName ? post.partnerName.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] text-[color:var(--app-muted-2)] uppercase font-bold">Người đăng</span>
              <span className="text-xs font-semibold text-[color:var(--app-text)] truncate max-w-[100px]">
                {post.partnerName || 'Chủ trọ'}
              </span>
            </div>
          </div>
          <button className="text-xs font-bold uppercase text-white bg-[color:var(--app-primary)] px-3 py-1.5 rounded-lg hover:bg-[color:var(--app-primary-hover)] transition-colors shadow-sm">
            Xem ngay
          </button>
        </div>
      </div>
    </Link>
  );
}