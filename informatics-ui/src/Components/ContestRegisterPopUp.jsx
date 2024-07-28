import { useState, useEffect, useContext } from 'react'
import { AxiosContext, getAxiosInstance } from '../utils/axiosInstance'
export default function ContestRegisterPopUp({contestId}){
    const axiosInstance = useContext(AxiosContext)
    const [text, setText] = useState("")
    useEffect(() => {
                    axiosInstance
                    .post(`/contest/${contestId}/register`, {
                    })
                    .then((response) => {
                        if(response.data.status == "SUCCESS"){
                            setText(" წარმატებით დარეგისტრირდი! 🎉")
                        }else{
                            setText("  კონტესტი უკვე დასრულებულია 🚫")
                        }
                        })
			.catch((error) => console.log(error))
	}, [])
    return (<>{text}</>)

}