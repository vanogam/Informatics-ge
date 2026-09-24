import ContestNavigationBar from "../Components/ContestNavigationBar";
import SubmissionsList from "../Components/SubmissionsList";

const GLOBAL_ROOM_ID = 1

export default function ArchiveStatus() {
    const getEndpoint = () => `/room/${GLOBAL_ROOM_ID}/status`

    return (
        <>
            <ContestNavigationBar />
            <SubmissionsList
                getEndpoint={getEndpoint}
                title="მცდელობები"
            />
        </>
    )
}
