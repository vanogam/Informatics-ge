import axios from 'axios'
import { toast } from 'react-toastify'
import getMessage from '../Components/lang'
import { AuthContext } from '../store/authentication'
import { createContext, useContext } from 'react'
import Cookies from 'js-cookie';

export const AxiosContext = createContext(null)

export const AxiosInstanceProvider = (props) => {
  const authContext = useContext(AuthContext)

  const getAxiosInstance = () => {

    const axiosInstance = axios.create({
        baseURL: `${process.env.REACT_APP_HOST}`,
        withCredentials: true,
        headers: {
          'Access-Control-Allow-Origin': `*`,
          'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept',
          'X-CSRF-Token': Cookies.get('XSRF-TOKEN') || '',
        },
      },
    );

    axiosInstance.interceptors.response.use((response) => {
      return response
    }, (error) => {
      const config = error.config || {};
      if (error.config.url === '/user') {
        return;
      }
      if (!config.ignoreErrors) {
        switch (error.response.status) {
          case 500:
            toast.error(getMessage('ka', 'unexpectedException'));
            break;
          case 403:
            toast.error(getMessage('ka', 'insufficientPrivileges'));
            break;
          case 401:
            if (error.config.url.endsWith('/submissions')) {
              return;
            }
            authContext.logout();
            toast.error(getMessage('ka', 'pleaseLogin'), { toastId: 'pleaseLogin' });
            break;
          case 400:
            toast.error(getMessage('ka', error.response.data.message))
        }
      }
      throw error
    })

    return axiosInstance
  }

  return (
    <AxiosContext.Provider value={getAxiosInstance()}>
      {props.children}
    </AxiosContext.Provider>
  )
}