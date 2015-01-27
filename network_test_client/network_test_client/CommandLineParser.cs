using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace network_test_client
{
    public enum UserAction
    {
        LicenseRequest,
        ExerciseScoreRequest,
        DiaryRequest,
        Exit
    }

    static class CommandLineParser
    {
        struct CommandLineOption
        {
            public CommandLineOption(UserAction _userAction
                , string _cmdIdentifier
                , string _description)
            {
                userAction = _userAction;
                cmdIdentifier = _cmdIdentifier;
                description = _description;
            }

            public UserAction userAction;
            public string cmdIdentifier;
            public string description;
        }

        private static CommandLineOption[] SupportedCommandLineOptions = new CommandLineOption[]
        {
            new CommandLineOption(UserAction.LicenseRequest, "license", "process license check request"),
            new CommandLineOption(UserAction.ExerciseScoreRequest, "exercise_score", "process exercise score request"),
            new CommandLineOption(UserAction.DiaryRequest, "diary", "get diary scores"),
            new CommandLineOption(UserAction.Exit, "exit", "exit from program")
        };

        private const string CmdUsageIdentifier = "\"{0}\": to {1}\n";
        
        public static UserAction ParseCommandLine(string argument)
        {
            if (string.IsNullOrEmpty(argument))
                throw new ArgumentException();
                        
            foreach (var cmdOption in SupportedCommandLineOptions)
            {
                if (string.Equals(cmdOption.cmdIdentifier, argument, StringComparison.InvariantCultureIgnoreCase))
                    return cmdOption.userAction;
            }
                      
            throw new ArgumentException();
        }

        public static string GetCommandLineUsage()
        {
            string resultString = string.Empty;
            foreach (var cmdOption in SupportedCommandLineOptions)
                resultString += string.Format(CmdUsageIdentifier, cmdOption.cmdIdentifier, cmdOption.description);
                        
            return resultString;
        }
    }
}
