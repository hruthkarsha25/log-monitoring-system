import { useState, useEffect } from "react";
import { fetchAllUsers } from "../services/authApi";

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAllUsers()
      .then(setUsers)
      .catch(e => console.error("Failed to load users", e))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <div style={{ background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.07)", borderRadius: 12, overflow: "hidden" }}>
        {/* Header */}
        <div style={{ padding: "10px 20px", display: "grid", gridTemplateColumns: "60px 1fr 1fr 100px", gap: 16, background: "rgba(255,255,255,0.03)", borderBottom: "1px solid rgba(255,255,255,0.06)" }}>
          {["ID", "USERNAME", "EMAIL", "ROLE"].map(h => (
            <span key={h} style={{ fontSize: 10, color: "#475569", fontFamily: "'JetBrains Mono'", fontWeight: 600, letterSpacing: "0.08em" }}>{h}</span>
          ))}
        </div>

        {loading ? (
          <div style={{ padding: 32, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>Loading...</div>
        ) : users.length === 0 ? (
          <div style={{ padding: 32, textAlign: "center", color: "#334155", fontFamily: "'JetBrains Mono'", fontSize: 12 }}>No users found</div>
        ) : users.map((user, i) => (
          <div key={user.id} style={{
            padding: "12px 20px", display: "grid", gridTemplateColumns: "60px 1fr 1fr 100px",
            gap: 16, alignItems: "center", borderBottom: i < users.length - 1 ? "1px solid rgba(255,255,255,0.03)" : "none",
          }}>
            <span style={{ fontSize: 11, color: "#475569", fontFamily: "'JetBrains Mono'" }}>#{user.id}</span>
            <span style={{ fontSize: 13, fontWeight: 600, color: "#e2e8f0" }}>{user.username}</span>
            <span style={{ fontSize: 12, color: "#64748b", fontFamily: "'JetBrains Mono'" }}>{user.email}</span>
            <span style={{
              fontSize: 11, fontFamily: "'JetBrains Mono'", fontWeight: 600,
              color: user.role === "ADMIN" ? "#fb923c" : "#38bdf8",
              background: user.role === "ADMIN" ? "rgba(251,146,60,0.1)" : "rgba(56,189,248,0.1)",
              border: `1px solid ${user.role === "ADMIN" ? "#fb923c33" : "#38bdf833"}`,
              padding: "2px 8px", borderRadius: 4, display: "inline-block"
            }}>{user.role}</span>
          </div>
        ))}
      </div>
    </div>
  );
}