function login() {
    var email = document.getElementById('email').value;
    var password = document.getElementById('password').value;

    fetch('/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email: email, password: password })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                window.location.href = '/mainpage';  // Redirect to main page
            } else {
                alert('Email or password is invalid');
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}
