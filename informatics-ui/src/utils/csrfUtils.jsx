import axios from "axios";
import Cookies from "js-cookie";

const renewCsrfToken = () => {
    axios.get('/api/csrf', {withCredentials: true})
        .then(
            (response) => {
                console.log(response)
                Cookies.set('XSRF-TOKEN', response.data);
            }
        ).catch((error) => {
        console.error('Error fetching CSRF token:', error);
    });
}

export { renewCsrfToken };