import React from "react";

const Footer = () => {
  return (
    <div className="footer-custom text-center p-3 border-top mt-4">
      &copy; {new Date().getFullYear()} TODO App. All rights reserved.
    </div>
  );
};

export default Footer;