import { createContext, useState } from "react";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {

  const [token, setToken] = useState(
    localStorage.getItem("accessToken") || null
  );

  const signIn = (jwt) => {
    localStorage.setItem("accessToken", jwt);
    setToken(jwt);
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        signIn,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};