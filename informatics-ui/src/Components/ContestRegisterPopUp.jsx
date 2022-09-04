import axios from "axios"
import { useState, useEffect } from "react"
export default function ContestRegisterPopUp({contestId}){
    const [text, setText] = useState("")
    useEffect(() => {
        setText(" წარმატებით დარეგისტრირდი! 🎉")
                    axios
                    .post(`http://localhost:8080/contest/${contestId}/register`, {
                    })
                    .then((response) => {
                        if(response.status == "SUCCESS"){
                            setText(" წარმატებით დარეგისტრირდი! 🎉")
                        }
                        })
			.catch((error) => console.log(error))
	}, [])
    return (<>{text}</>)

}