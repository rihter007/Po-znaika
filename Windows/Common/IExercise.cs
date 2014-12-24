using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace ru.po_znaika.common
{
    public interface IExercise
    {
        void Process();

        int GetId();
        string GetName();
        string GetDisplayName();
        Stream GetDisplayImage();
    }
}
