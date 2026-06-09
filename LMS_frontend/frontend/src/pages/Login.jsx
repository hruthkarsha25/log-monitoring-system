import { useState, useContext } from "react";
import { useNavigate, Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import api from "../services/axiosConfig";
import "../css/Auth.css";
import { toast } from "react-toastify";

const Login = () => {

  const [loginInput, setLoginInput] = useState("");
  const [password, setPassword] = useState("");

  const { signIn } = useContext(AuthContext);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {

    e.preventDefault();

    try {

      const response = await api.post("/auth/login", {
        login: loginInput,
        password,
      });

      const {
        accessToken,
        refreshToken,
        id,
        username,
        email,
        role,
      } = response.data;

      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);

      localStorage.setItem(
        "user",
        JSON.stringify({
          id,
          username,
          email,
          role,
        })
      );

      signIn(accessToken);

      toast.success("Login Successful");

      navigate("/dashboard");

      console.log("LOGIN RESPONSE", response.data);

    } catch (err) {
      toast.error(
        err.response?.data?.message ||
        "Invalid Credentials"
      );
      console.error(err);
    }
  };

  return (
    <div className="auth-container">

      <div className="auth-card">

        <h1>Log Monitoring System</h1>

        <h2>Login</h2>

        <form onSubmit={handleSubmit}>

          <input
            type="text"
            placeholder="Username or Email"
            value={loginInput}
            onChange={(e) =>
              setLoginInput(e.target.value)
            }
            required
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) =>
              setPassword(e.target.value)
            }
            required
          />

          <button type="submit">
            Login
          </button>

        </form>

        <p>
          Don't have an account?{" "}
          <Link to="/register">
            Register
          </Link>
        </p>

      </div>

    </div>
  );
};

export default Login;