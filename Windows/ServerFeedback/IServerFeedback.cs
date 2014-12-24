using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ru.po_znaika.server_feedback
{
    public interface IServerFeedback
    {
        bool ReportExerciseResult(int exerciseId, int score);
    }
}
