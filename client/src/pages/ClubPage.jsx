import ClubDiscussions from "../components/Discussions/ClubDiscussions";
import ClubBanner from "../components/Club/ClubBanner";
import ClubDetails from "../components/Club/ClubDetails";
import { fetchClub } from "@/services/clubService";
import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";

export default function ClubPage() {
  const { id } = useParams();
  const {
    data: club,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["club"],
    queryFn: () => fetchClub(id),
  });
 
  return (
    <div className="min-h-screen bg-gray-950">
      {isLoading ? (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-lg font-semibold text-gray-600 animate-pulse">
            Loading clubs...
          </div>
        </div>
      ) : error ? (
        <div className="flex items-center justify-center min-h-screen">
          <div className="p-4 text-red-500 bg-red-100 rounded-lg shadow">
            Error: {error.message}
          </div>
        </div>
      ) : (
        <>
          <ClubBanner imageUrl={club.imageUrl} />
          <div className="relative z-10 px-4 mx-auto -mt-52 max-w-7xl sm:px-6 lg:px-8">
            <ClubDetails club={club} />
            <ClubDiscussions id={id} />
          </div>
        </>
      )}
    </div>
  );
}
