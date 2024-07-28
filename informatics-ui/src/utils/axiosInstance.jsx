import axios from 'axios'
import { toast } from 'react-toastify'
import getMessage from '../Components/lang'
import { AuthContext } from '../store/authentication'
import { createContext, useContext } from 'react'
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
        },
      },
    );

    axiosInstance.interceptors.response.use((response) => {
      return response
    }, (error) => {
      switch (error.response.status) {
        case 500:
          toast.error(getMessage('ka', 'unexpectedException'));
          break;
        case 403:
          toast.error(getMessage('ka', 'insufficientPrivileges'));
          break;
        case 401:
          authContext.logout();
          toast.error(getMessage('ka', 'pleaseLogin'));
          break;
        case 400:
          console.log(error.response)
          toast.error(getMessage('ka', error.response.message))
      }
    })

    return axiosInstance
  }

  return (
    <AxiosContext.Provider value={getAxiosInstance()}>
      {props.children}
    </AxiosContext.Provider>
  )
}