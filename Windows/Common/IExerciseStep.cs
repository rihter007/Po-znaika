using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ru.po_znaika.common
{
    public interface IExerciseStep
    {
        void Process();

        void SetPreviousExerciseStep(IExerciseStep prevExerciseStep);
        void SetNextExerciseStep(IExerciseStep nextExerciseStep);
    }
}
