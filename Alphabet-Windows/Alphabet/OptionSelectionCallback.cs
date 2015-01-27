using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ru.po_znaika.alphabet
{
    public interface ISingleOptionSelectionCallback
    {
        void OnSelection(int variant);
    }

    public interface IMultipleOptionSelectionCallback
    {
        void OnSelection(IList<int> variant);
    }
}
