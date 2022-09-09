import axios from "axios"
import { useState, useEffect } from "react"
export default function ContestRegisterPopUp({contestId}){
    const [text, setText] = useState("")
    useEffect(() => {
                    axios
                    .post(`${process.env.REACT_APP_HOST}/contest/${contestId}/register`, {
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