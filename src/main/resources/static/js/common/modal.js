// This script assumes the modal HTML is present in the document.

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('passwordModal');
    // If the modal is not on the page, do nothing.
    if (!modal) {
        return;
    }

    const closeButton = modal.querySelector('.close-button');
    const confirmPasswordBtn = modal.querySelector('#confirmPasswordBtn');
    const passwordInput = modal.querySelector('#passwordInput');
    const modalErrorMessage = modal.querySelector('#modalErrorMessage');

    let successCallback = null; // To store the function to run on success

    // Make openModal globally accessible
    window.openModal = function(onSuccess) {
      successCallback = onSuccess;
      passwordInput.value = '';
      modalErrorMessage.textContent = '';
      modal.style.display = 'block';
      passwordInput.focus();
    }

    function closeModal() {
      modal.style.display = 'none';
      successCallback = null; // Reset callback
    }

    function handlePasswordConfirm() {
      const enteredPassword = passwordInput.value;
      // In a real app, you'd fetch to a server endpoint to verify the password.
      // We'll use a hardcoded password for this simulation.
      const correctPassword = "123";

      if (enteredPassword === correctPassword) {
        if (typeof successCallback === 'function') {
          // Execute the callback and then close the modal
          successCallback();
        }
        closeModal();
      } else if (enteredPassword === '') {
        modalErrorMessage.textContent = '비밀번호를 입력해주세요.';
      } else {
        modalErrorMessage.textContent = '비밀번호가 올바르지 않습니다.';
      }
    }

    // --- Event Listeners ---
    if (closeButton) {
        closeButton.onclick = closeModal;
    }

    if (confirmPasswordBtn) {
        confirmPasswordBtn.onclick = handlePasswordConfirm;
    }

    // Close modal if background is clicked
    window.addEventListener('click', function(event) {
      if (event.target == modal) {
        closeModal();
      }
    });

    // Handle 'Enter' key press in the password input
    if (passwordInput) {
        passwordInput.addEventListener('keyup', function(event) {
          if (event.key === 'Enter') {
            event.preventDefault();
            handlePasswordConfirm();
          }
        });
    }
}); 