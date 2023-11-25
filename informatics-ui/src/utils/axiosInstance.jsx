import axios from 'axios'

export const getAxiosInstance = () => {
    return axios.create({
            baseURL: `${process.env.REACT_APP_HOST}`,
      withCredentials: true,
            headers: {
                'Access-Control-Allow-Origin': `*`,
                'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept',
            },
        },
    )
}