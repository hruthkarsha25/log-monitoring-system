import { levelConfig } from "../services/dashboasdData";
import LevelBadge from "./LevelBadge";
import StatCard from "./StatCard";
import { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import Swal from "sweetalert2";

export default function Sidebar({
  activePage,
  setActivePage,
  alertCount,
}) {

  const { logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const user = JSON.parse(
    localStorage.getItem("user") || "{}"
  );

  const menuItems = [
    {
      id: "dashboard",
      icon: "▣",
      label: "Dashboard",
    },
    {
      id: "logs",
      icon: "☰",
      label: "Log Explorer",
    },
    {
      id: "alerts",
      icon: "◎",
      label: "Alerts",
      badge: alertCount,
    },
    {
      id: "services",
      icon: "◈",
      label: "Services",
    }
  ];

  // RBAC - Admin Only Pages
  if (user?.role === "ADMIN") {
    menuItems.push(
      {
        id: "users",
        icon: "👥",
        label: "Users",
      },
      {
        id: "audit",
        icon: "🔍",
        label: "Audit Logs",
      },
      {
        id: "settings",
        icon: "⚙",
        label: "Settings",
      }
    );
  }

  const handleLogout = async () => {
  const result = await Swal.fire({
    title: "Logout?",
    text: "Are you sure you want to logout?",
    icon: "question",
    showCancelButton: true,
    confirmButtonText: "Logout",
    cancelButtonText: "Cancel",
    confirmButtonColor: "#ef4444",
    background: "#111827",
    color: "#fff",
  });

  if (!result.isConfirmed) return;

  logout();

  Swal.fire({
    title: "Logged Out",
    text: "You have been logged out successfully",
    icon: "success",
    timer: 1500,
    showConfirmButton: false,
    background: "#111827",
    color: "#fff",
  });

  navigate("/login", { replace: true });
};

  return (
    <nav
      style={{
        width: 220,
        height: "100vh",

        position: "fixed",
        left: 0,
        top: 0,

        background: "rgba(255,255,255,0.02)",
        borderRight: "1px solid rgba(255,255,255,0.06)",

        display: "flex",
        flexDirection: "column",

        padding: "24px 12px",
        boxSizing: "border-box",

        overflow: "hidden",
        zIndex: 100,
      }}
    >
      {/* Logo */}
      <div
        style={{
          padding: "4px 12px 24px",
          display: "flex",
          alignItems: "center",
          gap: 10,
        }}
      >
        <div
          style={{
            width: 32,
            height: 32,
            background:
              "linear-gradient(135deg,#3b82f6,#8b5cf6)",
            borderRadius: 8,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontSize: 16,
          }}
        >
          📊
        </div>

        <div>
          <div
            style={{
              fontWeight: 700,
              fontSize: 14,
              letterSpacing: "-0.02em",
            }}
          >
            Log Monitoring
          </div>

          <div
            style={{
              color: "#475569",
              fontSize: 10,
              fontFamily: "'JetBrains Mono'",
              letterSpacing: "0.05em",
            }}
          >
          </div>
        </div>
      </div>

      {/* Navigation */}
    <div style={{ flex: 1 }}>
      {menuItems.map((item) => (
        <div
          key={item.id}
          className="nav-item"
          onClick={() => {
            if (item.id === "logout") {
              handleLogout();
            } else {
              setActivePage(item.id);
            }
          }}
          style={{
            padding: "10px 12px",
            borderRadius: 8,
            display: "flex",
            alignItems: "center",
            gap: 10,
            cursor: "pointer",
            background:
              activePage === item.id
                ? "rgba(59,130,246,0.15)"
                : "transparent",
            color:
              activePage === item.id
                ? "#60a5fa"
                : "#64748b",
            borderLeft:
              activePage === item.id
                ? "2px solid #3b82f6"
                : "2px solid transparent",
            transition: "all 0.2s ease",
          }}
        >
          <span style={{ fontSize: 14 }}>
            {item.icon}
          </span>

          <span
            style={{
              fontSize: 13,
              fontWeight: 500,
            }}
          >
            {item.label}
          </span>

          {item.badge > 0 && (
            <span
              style={{
                marginLeft: "auto",
                background: "#ef4444",
                color: "#fff",
                fontSize: 10,
                borderRadius: 10,
                padding: "1px 6px",
                fontFamily: "'JetBrains Mono'",
              }}
            >
              {item.badge}
            </span>
          )}
        </div>
      ))}
      </div>

      {/* User Card */}
      <div style={{
        marginTop: "auto",
      }}>
      <div
        style={{
          padding: "12px",
          borderRadius: 8,
          background: "rgba(255,255,255,0.03)",
          border: "1px solid rgba(255,255,255,0.06)",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 10,
          }}
        >
          <div
            style={{
              width: 32,
              height: 32,
              borderRadius: "50%",
              background:
                "linear-gradient(135deg,#6366f1,#8b5cf6)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: 13,
              fontWeight: 700,
              color: "#fff",
            }}
          >
            {user?.username
              ?.charAt(0)
              ?.toUpperCase() || "U"}
          </div>

          <div>
            <div
              style={{
                fontSize: 12,
                fontWeight: 600,
                color: "#e2e8f0",
              }}
            >
              {user?.username || "Guest"}
            </div>

            <div
              style={{
                fontSize: 10,
                color: "#475569",
                fontFamily: "'JetBrains Mono'",
                textTransform: "uppercase",
              }}
            >
              {user?.role || "USER"}
            </div>
          </div>
        </div>
      </div>

      <div
        className="nav-item"
        onClick={handleLogout}
        style={{
          marginTop: 12,
          padding: "10px 12px",
          borderRadius: 8,
          display: "flex",
          alignItems: "center",
          gap: 10,
          cursor: "pointer",
          color: "#ef4444",
          transition: "all 0.2s ease",
        }}
      >
        <span style={{ fontSize: 14 }}>🚪</span>
        <span
          style={{
            fontSize: 13,
            fontWeight: 500,
          }}
        >
          Logout
        </span>
      </div>
      </div>
    </nav>
  );
}