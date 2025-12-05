document.addEventListener('DOMContentLoaded', function () {
  const loginForm = document.querySelector('section');
  loginForm.style.opacity = 0;

  setTimeout(() => {
    loginForm.style.transition = 'opacity 1s ease-in-out';
    loginForm.style.opacity = 1;
  }, 500);

  const loginButton = document.querySelector('button');
  loginButton.addEventListener('click', function () {
    const emailInput = document.querySelector('input[type="email"]');
    const passwordInput = document.querySelector('input[type="password"]');

    // Validar email y contraseña
    const isValid = emailInput.checkValidity() && passwordInput.checkValidity();

    if (!isValid) {
      loginForm.classList.add('shake');

      setTimeout(() => {
        loginForm.classList.remove('shake');
      }, 1000);
    }
  });
});