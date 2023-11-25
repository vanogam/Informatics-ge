import { useState, useEffect } from "react"
import { getAxiosInstance } from '../utils/axiosInstance'
export default function ContestRegisterPopUp({contestId}){
    const [text, setText] = useState("")
    useEffect(() => {
                    getAxiosInstance()
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