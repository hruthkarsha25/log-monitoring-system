import React from "react";

export default function StatCard({
  label,
  value,
  sub,
  color,
  icon,
}) {
  return (
    <div
      style={{
        background: "rgba(255,255,255,0.03)",
        border: "1px solid rgba(255,255,255,0.07)",
        borderRadius: 12,
        padding: "20px 22px",
        display: "flex",
        flexDirection: "column",
        gap: 8,
        position: "relative",
        overflow: "hidden",
      }}
    >
      <div
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          right: 0,
          height: 2,
          background: color,
          borderRadius: "12px 12px 0 0",
          opacity: 0.7,
        }}
      />

      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
        }}
      >
        <span
          style={{
            color: "#94a3b8",
            fontSize: 12,
            fontFamily: "'JetBrains Mono', monospace",
            letterSpacing: "0.06em",
            textTransform: "uppercase",
          }}
        >
          {label}
        </span>

        <span style={{ fontSize: 20 }}>{icon}</span>
      </div>

      <div
        style={{
          color: "#f1f5f9",
          fontSize: 30,
          fontWeight: 700,
          fontFamily: "'Space Grotesk', sans-serif",
          letterSpacing: "-0.02em",
        }}
      >
        {value}
      </div>

      <div
        style={{
          color: "#64748b",
          fontSize: 12,
          fontFamily: "'JetBrains Mono', monospace",
        }}
      >
        {sub}
      </div>
    </div>
  );
}