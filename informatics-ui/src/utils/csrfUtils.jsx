import axios from "axios";
import Cookies from "js-cookie";

const renewCsrfToken = () => {
    axios.get('/api/csrf', {withCredentials: true})
        .then(
            (response) => {
                Cookies.set('XSRF-TOKEN', response.data);
            }
        ).catch((error) => {
    });
}

export { renewCsrfToken };