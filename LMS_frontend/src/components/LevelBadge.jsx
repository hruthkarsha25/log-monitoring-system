const levelConfig = {
  INFO: {
    bg: "rgba(56,189,248,0.12)",
    color: "#38bdf8",
  },
  WARN: {
    bg: "rgba(251,191,36,0.12)",
    color: "#fbbf24",
  },
  ERROR: {
    bg: "rgba(248,113,113,0.12)",
    color: "#f87171",
  }
};

export default function LevelBadge({ level }) {
  const cfg = levelConfig[level] || levelConfig.INFO;

  return (
    <span
      style={{
        background: cfg.bg,
        color: cfg.color,
        border: `1px solid ${cfg.color}33`,
        padding: "2px 9px",
        borderRadius: 4,
        fontSize: 11,
        fontFamily: "'JetBrains Mono', monospace",
        fontWeight: 600,
        letterSpacing: "0.05em",
      }}
    >
      {level}
    </span>
  );
}