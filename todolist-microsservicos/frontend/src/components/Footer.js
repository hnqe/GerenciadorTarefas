import React from "react";

const Footer = () => {
  return (
    <div
      className="text-center p-3 bg-light border-top mt-4" // Classe Bootstrap para margem
      style={{
        fontSize: "0.9rem",
        color: "#888",
      }}
    >
      &copy; {new Date().getFullYear()} TODO App. All rights reserved.
    </div>
  );
};

export default Footer;